package net.wizmagic.magic;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.config.WizConfig;
import net.wizmagic.item.WandItem;
import net.wizmagic.item.WandType;
import net.wizmagic.net.FxPayload;
import net.wizmagic.net.ManaPayload;
import net.wizmagic.net.ModNetwork;
import net.wizmagic.net.SelectSpellPayload;
import net.wizmagic.registry.ModSounds;
import net.wizmagic.util.WandMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Server side brain of the magic system: mana, cooldowns, spell selection, the short casting charge,
 * the spell projectiles in flight and the lightning auras.
 */
public final class SpellManager {
	private static final Map<UUID, PlayerMagic> PLAYERS = new HashMap<>();
	private static final List<Charge> CHARGES = new ArrayList<>();
	private static final List<SpellProjectile> PROJECTILES = new ArrayList<>();
	private static final List<Aura> AURAS = new ArrayList<>();
	private static long now = 0;

	private SpellManager() {
	}

	/** Per player magic state. */
	public static final class PlayerMagic {
		public float mana = -1f;
		public SpellType selected = SpellType.AQUA;
		public final long[] cooldownUntil = new long[SpellType.VALUES.length];
		public final int[] cooldownTotal = new int[SpellType.VALUES.length];
		public long lastManaUse = -1000;
		public boolean charging;
		public boolean dirty = true;
		public long lastSync = -1000;
		public float lastSentMana = -1f;
	}

	private static final class Charge {
		final ServerPlayer player;
		final SpellType spell;
		final WandType wand;
		final InteractionHand hand;
		final int total;
		int left;

		Charge(ServerPlayer player, SpellType spell, WandType wand, InteractionHand hand, int total) {
			this.player = player;
			this.spell = spell;
			this.wand = wand;
			this.hand = hand;
			this.total = total;
			this.left = total;
		}
	}

	private static final class Aura {
		final LivingEntity entity;
		final SpellType spell;
		int left;

		Aura(LivingEntity entity, SpellType spell, int left) {
			this.entity = entity;
			this.spell = spell;
			this.left = left;
		}
	}

	public static void init() {
		ServerTickEvents.END_SERVER_TICK.register(SpellManager::tick);
		ServerPlayNetworking.registerGlobalReceiver(SelectSpellPayload.TYPE,
				(payload, context) -> select(context.player(), payload.index()));
	}

	public static PlayerMagic get(ServerPlayer player) {
		PlayerMagic m = PLAYERS.computeIfAbsent(player.getUUID(), id -> new PlayerMagic());
		float max = WizConfig.INSTANCE.general.maxMana;
		if (m.mana < 0f) {
			m.mana = max;
		}
		if (m.mana > max) {
			m.mana = max;
		}
		return m;
	}

	public static void select(ServerPlayer player, int index) {
		PlayerMagic m = get(player);
		SpellType spell = SpellType.byIndex(index);
		if (m.selected != spell) {
			m.selected = spell;
			m.dirty = true;
			ModSounds.play((ServerLevel) player.level(), player.position(), ModSounds.SELECT, 0.45f, 0.9f + 0.12f * spell.ordinal());
		}
	}

	public static void addAura(LivingEntity entity, SpellType spell, int ticks) {
		for (Aura a : AURAS) {
			if (a.entity == entity && a.spell == spell) {
				a.left = Math.max(a.left, ticks);
				return;
			}
		}
		AURAS.add(new Aura(entity, spell, ticks));
	}

	private static boolean isFree(ServerPlayer player) {
		return WizConfig.INSTANCE.general.creativeIgnoresCosts && player.getAbilities().instabuild;
	}

	/** Called when a player right-clicks with a wand. */
	public static void tryCast(ServerPlayer player, InteractionHand hand, WandType wand) {
		PlayerMagic m = get(player);
		if (m.charging) {
			return;
		}
		SpellType spell = m.selected;
		int index = spell.ordinal();
		ServerLevel level = (ServerLevel) player.level();
		boolean free = isFree(player);

		if (!free && now < m.cooldownUntil[index]) {
			return;
		}
		float cost = spell.manaCost();
		if (!free && m.mana < cost) {
			player.displayClientMessage(Component.translatable("message.wizmagic.no_mana"), true);
			ModSounds.play(level, player.position(), ModSounds.NO_MANA, 0.6f, 1.0f);
			return;
		}

		if (!free) {
			m.mana -= cost;
			m.lastManaUse = now;
			m.cooldownUntil[index] = now + spell.cooldownTicks() + spell.chargeTicks();
			m.cooldownTotal[index] = spell.cooldownTicks() + spell.chargeTicks();
		}
		m.charging = true;
		m.dirty = true;
		CHARGES.add(new Charge(player, spell, wand, hand, spell.chargeTicks()));
		ModSounds.play(level, player.position(), ModSounds.CHARGE[index], 0.9f, 1.0f);
	}

	private static void tick(MinecraftServer server) {
		now++;

		// mana, sync
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			PlayerMagic m = get(player);
			WizConfig.General g = WizConfig.INSTANCE.general;
			if (now - m.lastManaUse >= g.regenDelayTicks && m.mana < g.maxMana) {
				m.mana = Math.min(g.maxMana, m.mana + g.manaRegenPerSecond / 20f);
			}
			if (m.dirty || (now - m.lastSync >= 10 && Math.abs(m.mana - m.lastSentMana) > 0.01f)) {
				sync(player, m);
			}
		}

		tickCharges();

		Iterator<SpellProjectile> it = PROJECTILES.iterator();
		while (it.hasNext()) {
			SpellProjectile p = it.next();
			p.tick();
			if (p.isFinished()) {
				it.remove();
			}
		}

		tickAuras();

		if (now % 400 == 0) {
			PLAYERS.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
		}
	}

	private static void sync(ServerPlayer player, PlayerMagic m) {
		short[] cd = new short[4];
		short[] total = new short[4];
		for (int i = 0; i < 4; i++) {
			cd[i] = (short) Math.max(0, Math.min(32000, m.cooldownUntil[i] - now));
			total[i] = (short) Math.max(1, Math.min(32000, m.cooldownTotal[i] == 0
					? SpellType.VALUES[i].cooldownTicks() + SpellType.VALUES[i].chargeTicks() : m.cooldownTotal[i]));
		}
		ServerPlayNetworking.send(player, new ManaPayload(m.mana, WizConfig.INSTANCE.general.maxMana,
				(byte) m.selected.ordinal(), cd[0], cd[1], cd[2], cd[3], total[0], total[1], total[2], total[3]));
		m.dirty = false;
		m.lastSync = now;
		m.lastSentMana = m.mana;
	}

	private static void tickCharges() {
		Iterator<Charge> it = CHARGES.iterator();
		while (it.hasNext()) {
			Charge c = it.next();
			ServerPlayer player = c.player;
			PlayerMagic m = get(player);

			ItemStack held = player.getItemInHand(c.hand);
			if (player.isRemoved() || !player.isAlive() || !(held.getItem() instanceof WandItem)) {
				// interrupted: give half of the mana back
				if (!isFree(player)) {
					m.mana = Math.min(WizConfig.INSTANCE.general.maxMana, m.mana + c.spell.manaCost() * 0.5f);
				}
				m.charging = false;
				m.dirty = true;
				it.remove();
				continue;
			}

			ServerLevel level = (ServerLevel) player.level();
			Vec3 tip = WandMath.tip(player, c.hand);
			Vec3 look = player.getLookAngle();
			float progress = 1f - (float) c.left / (float) c.total;
			ModNetwork.sendFx(level, FxPayload.CHARGE, c.spell, c.wand.ordinal(), tip, look, progress);

			c.left--;
			if (c.left <= 0) {
				release(player, c, level);
				m.charging = false;
				m.dirty = true;
				it.remove();
			}
		}
	}

	private static float rangeOf(SpellType spell) {
		WizConfig cfg = WizConfig.INSTANCE;
		return switch (spell) {
			case AQUA -> cfg.aqua.range;
			case LIGHTNING -> cfg.lightning.range;
			case INFERNO -> cfg.inferno.range;
			case EXPULSO -> cfg.expulso.range;
		};
	}

	private static void release(ServerPlayer player, Charge c, ServerLevel level) {
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getLookAngle();
		double range = rangeOf(c.spell);
		Vec3 far = eye.add(look.scale(range));

		// where the crosshair points
		BlockHitResult aim = level.clip(new ClipContext(eye, far, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		Vec3 target = aim.getType() == HitResult.Type.MISS ? far : aim.getLocation();

		// the wand may poke into a wall: never start inside it
		Vec3 start = WandMath.tip(player, c.hand);
		BlockHitResult wall = level.clip(new ClipContext(eye, start, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		if (wall.getType() != HitResult.Type.MISS) {
			start = eye.add(start.subtract(eye).scale(0.4));
		}

		Vec3 dir = target.subtract(start);
		if (dir.lengthSqr() < 4.0) {
			dir = look;
		}
		dir = dir.normalize();

		int wandOrdinal = c.wand.ordinal();
		player.swing(c.hand, true);
		ModSounds.play(level, start, ModSounds.CAST[c.spell.ordinal()], 1.1f, 0.95f + level.getRandom().nextFloat() * 0.1f);
		ModNetwork.sendFx(level, FxPayload.RELEASE, c.spell, wandOrdinal, start, dir, 1f);

		SpellProjectile projectile = switch (c.spell) {
			case AQUA -> new AquaBolt(level, player, c.hand, wandOrdinal, start, dir);
			case LIGHTNING -> new ArcBolt(level, player, c.hand, wandOrdinal, start, dir);
			case INFERNO -> new InfernoOrb(level, player, c.hand, wandOrdinal, start, dir);
			case EXPULSO -> new ExpulsoBolt(level, player, c.hand, wandOrdinal, start, dir);
		};
		PROJECTILES.add(projectile);
	}

	private static void tickAuras() {
		Iterator<Aura> it = AURAS.iterator();
		while (it.hasNext()) {
			Aura a = it.next();
			if (a.entity.isRemoved() || !a.entity.isAlive() || a.left <= 0) {
				it.remove();
				continue;
			}
			if (a.left % 2 == 0 && a.entity.level() instanceof ServerLevel level) {
				ModNetwork.sendFx(level, FxPayload.AURA, a.spell, a.entity.position(),
						new Vec3(a.entity.getBbWidth(), a.entity.getBbHeight(), 0), a.left);
			}
			a.left--;
		}
	}
}
