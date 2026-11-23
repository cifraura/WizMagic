package net.wizmagic.magic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.config.WizConfig;
import net.wizmagic.net.FxPayload;
import net.wizmagic.net.ModNetwork;
import net.wizmagic.registry.ModSounds;
import net.wizmagic.util.WandMath;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Lightning Arc: a very fast beam head. Each tick the client draws a freshly randomised branching beam from the
 * wand to the head, so the lightning crackles and animates. After impact the beam lingers briefly.
 */
public class ArcBolt extends SpellProjectile {
	private int linger = -1;
	private Vec3 hitPoint = Vec3.ZERO;

	public ArcBolt(ServerLevel level, Player owner, InteractionHand hand, int wandOrdinal, Vec3 start, Vec3 dir) {
		super(level, owner, hand, SpellType.LIGHTNING, wandOrdinal, start, dir,
				WizConfig.INSTANCE.lightning.speed, WizConfig.INSTANCE.lightning.range);
	}

	@Override
	public void tick() {
		if (linger >= 0) {
			Player owner = level.getPlayerByUUID(ownerId);
			if (owner == null) {
				finished = true;
				return;
			}
			ModNetwork.sendFx(level, FxPayload.ARC, spell, wandOrdinal, WandMath.tip(owner, hand), hitPoint, 0f);
			linger--;
			if (linger < 0) {
				finished = true;
			}
			return;
		}
		super.tick();
	}

	@Override
	protected void onStep(Player owner, Vec3 from, Vec3 to, double travelledBefore) {
		ModNetwork.sendFx(level, FxPayload.ARC, spell, wandOrdinal, WandMath.tip(owner, hand), to, 0f);
	}

	private void startLinger(Vec3 point) {
		hitPoint = point;
		linger = 3;
		finished = false;
	}

	@Override
	protected void onEntityHit(Player owner, LivingEntity target, Vec3 hit) {
		startLinger(hit);
		strike(owner, target, hit);
	}

	@Override
	protected void onBlockHit(Player owner, BlockHitResult hit, Vec3 point) {
		Vec3 p = offsetFromFace(hit, point, 0.2);
		startLinger(p);
		strike(owner, null, p);
	}

	@Override
	protected void onExpire(Player owner, Vec3 point) {
		startLinger(point);
		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, point, Vec3.ZERO, 0.8f);
	}

	private void strike(Player owner, LivingEntity first, Vec3 hit) {
		WizConfig.Lightning c = WizConfig.INSTANCE.lightning;
		List<LivingEntity> struck = new ArrayList<>();
		Vec3 from = hit;
		float damage = c.damage;

		if (first != null) {
			zap(owner, first, damage);
			struck.add(first);
			from = SpellEffects.center(first);
		}
		for (int i = 0; i < c.chainTargets; i++) {
			LivingEntity next = nearest(owner, from, struck, c.chainRadius);
			if (next == null) {
				break;
			}
			damage *= c.chainDamageFactor;
			Vec3 to = SpellEffects.center(next);
			ModNetwork.sendFx(level, FxPayload.ARC, spell, wandOrdinal, from, to, 1f);
			zap(owner, next, damage);
			struck.add(next);
			from = to;
		}

		Vec3 normal = dir.scale(-1);
		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, hit, normal, 3.0f);
		ModSounds.play(level, hit, ModSounds.IMPACT[spell.ordinal()], 1.3f, 0.95f + level.getRandom().nextFloat() * 0.1f);
	}

	private void zap(Player owner, LivingEntity target, float damage) {
		WizConfig.Lightning c = WizConfig.INSTANCE.lightning;
		SpellEffects.hurt(level, target, owner, damage);
		SpellEffects.stun(target, c.stunTicks);
		SpellManager.addAura(target, spell, c.auraTicks);
		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, SpellEffects.center(target), Vec3.ZERO, 1.1f);
	}

	private LivingEntity nearest(Player owner, Vec3 from, List<LivingEntity> already, double radius) {
		List<LivingEntity> candidates = new ArrayList<>(SpellEffects.livingInRadius(level, from, radius, owner));
		candidates.removeAll(already);
		candidates.sort(Comparator.comparingDouble(e -> SpellEffects.center(e).distanceToSqr(from)));
		return candidates.isEmpty() ? null : candidates.get(0);
	}
}
