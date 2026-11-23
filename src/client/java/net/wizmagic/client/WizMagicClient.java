package net.wizmagic.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.WizMagic;
import net.wizmagic.client.fx.ClientEffects;
import net.wizmagic.client.fx.Fx;
import net.wizmagic.client.fx.FxPlayer;
import net.wizmagic.client.gui.SpellHud;
import net.wizmagic.client.gui.SpellSelectScreen;
import net.wizmagic.client.particle.ClientParticles;
import net.wizmagic.config.WizConfig;
import net.wizmagic.item.WandItem;
import net.wizmagic.magic.SpellType;
import net.wizmagic.net.FxPayload;
import net.wizmagic.net.ManaPayload;
import net.wizmagic.net.SelectSpellPayload;
import net.wizmagic.util.WandMath;

public class WizMagicClient implements ClientModInitializer {
	private static final RandomSource RANDOM = RandomSource.create();

	@Override
	public void onInitializeClient() {
		ClientParticles.register();
		ClientKeys.register();

		ClientPlayNetworking.registerGlobalReceiver(FxPayload.TYPE, (payload, context) -> FxPlayer.handle(payload));
		ClientPlayNetworking.registerGlobalReceiver(ManaPayload.TYPE, (payload, context) -> ClientMagicState.apply(payload));

		HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, WizMagic.id("spell_hud"), SpellHud::render);

		ClientTickEvents.END_CLIENT_TICK.register(WizMagicClient::tick);
	}

	private static void tick(Minecraft mc) {
		ClientMagicState.tick();
		ClientEffects.tick();

		if (mc.level == null || mc.player == null) {
			ClientEffects.clear();
			return;
		}

		for (SpellType spell : SpellType.VALUES) {
			while (ClientKeys.SELECT[spell.ordinal()].consumeClick()) {
				ClientPlayNetworking.send(new SelectSpellPayload((byte) spell.ordinal()));
				ClientMagicState.selectLocally(spell.ordinal());
			}
		}
		while (ClientKeys.MENU.consumeClick()) {
			mc.setScreen(new SpellSelectScreen());
		}

		if (WizConfig.INSTANCE.client.wandAmbientGlow) {
			ambientGlow(mc.level);
		}
	}

	/** Players holding a wand have a softly glowing, sparkling wand tip in the wand's own colour. */
	private static void ambientGlow(ClientLevel level) {
		long time = level.getGameTime();
		for (AbstractClientPlayer player : level.players()) {
			if (player.isInvisible() || player.isSpectator()) {
				continue;
			}
			for (InteractionHand hand : InteractionHand.values()) {
				ItemStack stack = player.getItemInHand(hand);
				if (!(stack.getItem() instanceof WandItem wand)) {
					continue;
				}
				int color = wand.getWandType().glow;
				Vec3 tip = WandMath.tip(player, hand);
				if (time % 3 == 0) {
					Fx.p("wand_glow").at(tip).size(0.09f).life(10).color(color).alpha(0.7f).spawn();
				}
				if (RANDOM.nextInt(Math.max(1, Math.round(6 / Math.max(0.25f, Fx.density())))) == 0) {
					Vec3 d = new Vec3(RANDOM.nextGaussian(), RANDOM.nextGaussian(), RANDOM.nextGaussian()).scale(0.03);
					Fx.p("wand_spark").at(tip).vel(d.add(0, 0.01, 0)).color(color).spawn();
				}
			}
		}
	}
}
