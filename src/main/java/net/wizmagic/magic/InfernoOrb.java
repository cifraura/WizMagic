package net.wizmagic.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.config.WizConfig;
import net.wizmagic.net.FxPayload;
import net.wizmagic.net.ModNetwork;
import net.wizmagic.registry.ModSounds;

/** Inferno: a blazing orb that burns targets, ignites blocks and bursts into a ring of magical fire. */
public class InfernoOrb extends SpellProjectile {

	public InfernoOrb(ServerLevel level, Player owner, InteractionHand hand, int wandOrdinal, Vec3 start, Vec3 dir) {
		super(level, owner, hand, SpellType.INFERNO, wandOrdinal, start, dir,
				WizConfig.INSTANCE.inferno.speed, WizConfig.INSTANCE.inferno.range);
	}

	@Override
	protected void onStep(Player owner, Vec3 from, Vec3 to, double travelledBefore) {
		super.onStep(owner, from, to, travelledBefore);
		// water puts the orb out
		if (level.getFluidState(BlockPos.containing(to)).is(FluidTags.WATER)) {
			finished = true;
			level.playSound((Player) null, to.x, to.y, to.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0f, 1.0f);
			ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, to, Vec3.ZERO, 0.8f);
		}
	}

	@Override
	protected void onEntityHit(Player owner, LivingEntity target, Vec3 hit) {
		burst(owner, SpellEffects.center(target), dir.scale(-1), target);
	}

	@Override
	protected void onBlockHit(Player owner, BlockHitResult hit, Vec3 point) {
		burst(owner, offsetFromFace(hit, point, 0.3), normalOf(hit), null);
	}

	private void burst(Player owner, Vec3 center, Vec3 normal, LivingEntity direct) {
		WizConfig.Inferno c = WizConfig.INSTANCE.inferno;
		double radius = c.blastRadius;

		if (direct != null) {
			SpellEffects.hurt(level, direct, owner, c.damage);
			direct.igniteForSeconds((float) c.burnSeconds);
		}
		for (LivingEntity e : SpellEffects.livingInRadius(level, center, radius, owner)) {
			if (e == direct) {
				continue;
			}
			double d = SpellEffects.center(e).distanceTo(center);
			float falloff = (float) Math.max(0.0, 1.0 - d / (radius + 0.5));
			SpellEffects.hurt(level, e, owner, c.damage * 0.7f * falloff);
			e.igniteForSeconds(Math.max(2f, c.burnSeconds * (0.4f + 0.6f * falloff)));
			SpellEffects.push(e, SpellEffects.center(e).subtract(center), 0.5 * falloff, 0.2 * falloff);
		}
		if (c.igniteBlocks) {
			SpellEffects.igniteBlocks(level, center, radius * 0.8, 16);
		}
		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, center, normal, (float) radius);
		ModSounds.play(level, center, ModSounds.IMPACT[spell.ordinal()], 1.3f, 0.95f + level.getRandom().nextFloat() * 0.1f);
	}
}
