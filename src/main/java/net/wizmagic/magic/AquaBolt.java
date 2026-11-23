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

/** Aqua Magic: a jet of water that pushes, slows and extinguishes. */
public class AquaBolt extends SpellProjectile {

	public AquaBolt(ServerLevel level, Player owner, InteractionHand hand, int wandOrdinal, Vec3 start, Vec3 dir) {
		super(level, owner, hand, SpellType.AQUA, wandOrdinal, start, dir,
				WizConfig.INSTANCE.aqua.speed, WizConfig.INSTANCE.aqua.range);
	}

	@Override
	protected void onStep(Player owner, Vec3 from, Vec3 to, double travelledBefore) {
		super.onStep(owner, from, to, travelledBefore);
		SpellEffects.extinguish(level, to, 1.3);
	}

	@Override
	protected void onEntityHit(Player owner, LivingEntity target, Vec3 hit) {
		WizConfig.Aqua c = WizConfig.INSTANCE.aqua;
		SpellEffects.hurt(level, target, owner, c.damage);
		SpellEffects.push(target, dir, c.knockback, 0.35);
		SpellEffects.slow(target, c.slowTicks, c.slowAmplifier);
		target.clearFire();
		splash(owner, hit, dir.scale(-1), target);
	}

	@Override
	protected void onBlockHit(Player owner, BlockHitResult hit, Vec3 point) {
		splash(owner, offsetFromFace(hit, point, 0.25), normalOf(hit), null);
	}

	private void splash(Player owner, Vec3 center, Vec3 normal, LivingEntity direct) {
		WizConfig.Aqua c = WizConfig.INSTANCE.aqua;
		double radius = c.splashRadius;
		for (LivingEntity e : SpellEffects.livingInRadius(level, center, radius, owner)) {
			if (e == direct) {
				continue;
			}
			double d = SpellEffects.center(e).distanceTo(center);
			double falloff = Math.max(0.0, 1.0 - d / (radius + 0.5));
			Vec3 away = SpellEffects.center(e).subtract(center);
			SpellEffects.push(e, away, c.knockback * 0.6 * falloff, 0.25 * falloff);
			SpellEffects.slow(e, c.slowTicks / 2, 0);
			e.clearFire();
		}
		SpellEffects.extinguish(level, center, radius);
		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, center, normal, (float) radius);
		ModSounds.play(level, center, ModSounds.IMPACT[spell.ordinal()], 1.2f, 0.95f + level.getRandom().nextFloat() * 0.1f);
	}
}
