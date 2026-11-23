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

/** Expulso: a fast violet bolt that detonates in a custom multi stage magical explosion. */
public class ExpulsoBolt extends SpellProjectile {

	public ExpulsoBolt(ServerLevel level, Player owner, InteractionHand hand, int wandOrdinal, Vec3 start, Vec3 dir) {
		super(level, owner, hand, SpellType.EXPULSO, wandOrdinal, start, dir,
				WizConfig.INSTANCE.expulso.speed, WizConfig.INSTANCE.expulso.range);
	}

	@Override
	protected void onEntityHit(Player owner, LivingEntity target, Vec3 hit) {
		explode(owner, hit, dir.scale(-1));
	}

	@Override
	protected void onBlockHit(Player owner, BlockHitResult hit, Vec3 point) {
		explode(owner, offsetFromFace(hit, point, 0.35), normalOf(hit));
	}

	private void explode(Player owner, Vec3 center, Vec3 normal) {
		WizConfig.Expulso c = WizConfig.INSTANCE.expulso;
		double radius = c.explosionRadius;

		for (LivingEntity e : SpellEffects.livingInRadius(level, center, radius, owner)) {
			Vec3 toEntity = SpellEffects.center(e).subtract(center);
			double d = toEntity.length();
			float falloff = (float) Math.max(0.0, 1.0 - d / (radius + 0.5));
			SpellEffects.hurt(level, e, owner, c.damage * falloff);
			Vec3 away = d < 1.0E-4 ? new Vec3(dir.x, 0, dir.z) : toEntity;
			SpellEffects.push(e, away, c.knockback * (0.4 + 0.6 * falloff), 0.35 + 0.5 * falloff);
		}
		SpellEffects.destroyBlocks(level, owner, center, radius);

		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, center, normal, (float) radius);
		ModSounds.play(level, center, ModSounds.IMPACT[spell.ordinal()], 1.8f, 0.95f + level.getRandom().nextFloat() * 0.1f);
	}
}
