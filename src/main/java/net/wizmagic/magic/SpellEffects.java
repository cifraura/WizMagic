package net.wizmagic.magic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.config.WizConfig;

import java.util.ArrayList;
import java.util.List;

/** Gameplay helpers shared by all spells: damage, knockback, fire, block destruction. */
public final class SpellEffects {
	private SpellEffects() {
	}

	public static Vec3 center(Entity e) {
		return e.position().add(0, e.getBbHeight() * 0.5, 0);
	}

	public static boolean hurt(ServerLevel level, LivingEntity target, Player owner, float amount) {
		DamageSource source = level.damageSources().indirectMagic(owner, owner);
		return target.hurtServer(level, source, amount);
	}

	/** Horizontal push plus a little lift, reduced by knockback resistance. */
	public static void push(LivingEntity target, Vec3 direction, double strength, double lift) {
		double resist = Mth.clamp(target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
		Vec3 flat = new Vec3(direction.x, 0, direction.z);
		if (flat.lengthSqr() < 1.0E-6) {
			flat = new Vec3(0, 0, 0);
		} else {
			flat = flat.normalize();
		}
		double scale = strength * (1.0 - resist);
		target.setDeltaMovement(target.getDeltaMovement().add(flat.x * scale, lift * (1.0 - resist), flat.z * scale));
		target.hurtMarked = true;
	}

	public static void slow(LivingEntity target, int ticks, int amplifier) {
		target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, amplifier, false, true, true));
	}

	/** Stun = slowness so strong that the target cannot walk, for a few ticks. */
	public static void stun(LivingEntity target, int ticks) {
		target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, ticks, 9, false, false, true));
	}

	public static List<LivingEntity> livingInRadius(ServerLevel level, Vec3 c, double r, Entity exclude) {
		AABB box = new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
		List<LivingEntity> result = new ArrayList<>();
		for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, le -> le.isAlive() && !le.isSpectator())) {
			if (e == exclude) {
				continue;
			}
			if (center(e).distanceTo(c) <= r + e.getBbWidth() * 0.5) {
				result.add(e);
			}
		}
		return result;
	}

	/** Puts out fires, campfires and burning entities. Returns how many blocks were extinguished. */
	public static int extinguish(ServerLevel level, Vec3 c, double r) {
		int count = 0;
		int ri = (int) Math.ceil(r);
		BlockPos base = BlockPos.containing(c);
		for (int dx = -ri; dx <= ri; dx++) {
			for (int dy = -ri; dy <= ri; dy++) {
				for (int dz = -ri; dz <= ri; dz++) {
					if (dx * dx + dy * dy + dz * dz > r * r) {
						continue;
					}
					BlockPos p = base.offset(dx, dy, dz);
					BlockState s = level.getBlockState(p);
					if (s.is(BlockTags.FIRE)) {
						level.removeBlock(p, false);
						count++;
					} else if (s.hasProperty(BlockStateProperties.LIT) && s.is(BlockTags.CAMPFIRES)
							&& s.getValue(BlockStateProperties.LIT)) {
						level.setBlock(p, s.setValue(BlockStateProperties.LIT, false), 3);
						count++;
					}
				}
			}
		}
		for (LivingEntity e : livingInRadius(level, c, r, null)) {
			if (e.isOnFire()) {
				e.clearFire();
				count++;
			}
		}
		if (count > 0) {
			level.playSound((Player) null, c.x, c.y, c.z, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.7f, 1.0f);
		}
		return count;
	}

	/** Tries to set fire to random free blocks around a point. */
	public static void igniteBlocks(ServerLevel level, Vec3 c, double r, int attempts) {
		RandomSource random = level.getRandom();
		for (int i = 0; i < attempts; i++) {
			BlockPos p = BlockPos.containing(
					c.x + (random.nextDouble() - 0.5) * 2 * r,
					c.y + (random.nextDouble() - 0.5) * 2 * r,
					c.z + (random.nextDouble() - 0.5) * 2 * r);
			if (!level.getBlockState(p).isAir()) {
				continue;
			}
			BlockState fire = BaseFireBlock.getState(level, p);
			if (fire.canSurvive(level, p)) {
				level.setBlockAndUpdate(p, fire);
			}
		}
	}

	private static boolean allowedToBreak(BlockState state, ServerLevel level, BlockPos pos, WizConfig.Expulso c) {
		if (state.isAir() || state.getBlock() instanceof LiquidBlock) {
			return false;
		}
		float hardness = state.getDestroySpeed(level, pos);
		if (hardness < 0 || hardness > c.maxBlockHardness) {
			return false;
		}
		String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
		if (!c.blockWhitelist.isEmpty()) {
			return c.blockWhitelist.contains(id);
		}
		if (c.blockBlacklist.contains(id)) {
			return false;
		}
		// containers and other block entities are protected by default
		return level.getBlockEntity(pos) == null;
	}

	/** Destroys blocks in a rough sphere, rarer towards the edge. Honours the config limits. */
	public static int destroyBlocks(ServerLevel level, Player owner, Vec3 c, double r) {
		WizConfig.Expulso cfg = WizConfig.INSTANCE.expulso;
		if (!cfg.destroyBlocks) {
			return 0;
		}
		RandomSource random = level.getRandom();
		int destroyed = 0;
		int ri = (int) Math.ceil(r);
		BlockPos base = BlockPos.containing(c);
		for (int dx = -ri; dx <= ri && destroyed < 700; dx++) {
			for (int dy = -ri; dy <= ri && destroyed < 700; dy++) {
				for (int dz = -ri; dz <= ri && destroyed < 700; dz++) {
					double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
					if (d > r) {
						continue;
					}
					double chance = 1.0 - (d / r) * 0.65;
					if (random.nextDouble() > chance) {
						continue;
					}
					BlockPos p = base.offset(dx, dy, dz);
					BlockState s = level.getBlockState(p);
					if (!allowedToBreak(s, level, p, cfg)) {
						continue;
					}
					if (!level.mayInteract(owner, p)) {
						continue;
					}
					if (level.destroyBlock(p, cfg.dropBlocks, owner)) {
						destroyed++;
					}
				}
			}
		}
		return destroyed;
	}
}
