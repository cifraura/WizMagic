package net.wizmagic.magic;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.net.FxPayload;
import net.wizmagic.net.ModNetwork;

import java.util.Optional;
import java.util.UUID;

/**
 * A spell projectile simulated purely on the server (no vanilla entity). It moves in sub-block steps, raycasts
 * against blocks and entities, and sends compact effect messages to the clients, which draw everything.
 */
public abstract class SpellProjectile {
	protected final ServerLevel level;
	protected final UUID ownerId;
	protected final InteractionHand hand;
	protected final SpellType spell;
	protected final int wandOrdinal;
	protected final Vec3 dir;
	protected final double speed;
	protected final double maxRange;
	protected Vec3 pos;
	protected double travelled;
	protected int age;
	protected boolean finished;

	protected SpellProjectile(ServerLevel level, Player owner, InteractionHand hand, SpellType spell, int wandOrdinal,
							  Vec3 start, Vec3 dir, double speed, double maxRange) {
		this.level = level;
		this.ownerId = owner.getUUID();
		this.hand = hand;
		this.spell = spell;
		this.wandOrdinal = wandOrdinal;
		this.pos = start;
		this.dir = dir.normalize();
		this.speed = speed;
		this.maxRange = maxRange;
	}

	public boolean isFinished() {
		return finished;
	}

	public void tick() {
		if (finished) {
			return;
		}
		Player owner = level.getPlayerByUUID(ownerId);
		if (owner == null || !owner.isAlive()) {
			finished = true;
			return;
		}
		age++;

		double step = Math.min(speed, maxRange - travelled);
		if (step <= 1.0E-4) {
			finished = true;
			onExpire(owner, pos);
			return;
		}

		Vec3 from = pos;
		Vec3 to = from.add(dir.scale(step));

		BlockHitResult blockHit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
				ClipContext.Fluid.NONE, owner));
		boolean hitBlock = blockHit.getType() == HitResult.Type.BLOCK;
		Vec3 end = hitBlock ? blockHit.getLocation() : to;

		LivingEntity hitEntity = null;
		double best = from.distanceToSqr(end) + 1.0E-6;
		AABB search = new AABB(from, end).inflate(1.0);
		for (Entity e : level.getEntities(owner, search,
				entity -> entity instanceof LivingEntity living && living.isAlive() && !living.isSpectator())) {
			AABB box = e.getBoundingBox().inflate(0.3);
			Optional<Vec3> clip = box.clip(from, end);
			Vec3 point = null;
			if (clip.isPresent()) {
				point = clip.get();
			} else if (box.contains(from)) {
				point = from;
			}
			if (point != null) {
				double d = from.distanceToSqr(point);
				if (d <= best) {
					best = d;
					hitEntity = (LivingEntity) e;
					end = point;
				}
			}
		}

		onStep(owner, from, end, travelled);
		travelled += from.distanceTo(end);
		pos = end;
		if (finished) {
			return; // the subclass ended the projectile while stepping (e.g. fire orb dying in water)
		}

		if (hitEntity != null) {
			finished = true;
			onEntityHit(owner, hitEntity, end);
		} else if (hitBlock) {
			finished = true;
			onBlockHit(owner, blockHit, end);
		} else if (travelled >= maxRange - 1.0E-3) {
			finished = true;
			onExpire(owner, end);
		}
	}

	/** Called every tick for the segment just travelled. Used to send trail effects. */
	protected void onStep(Player owner, Vec3 from, Vec3 to, double travelledBefore) {
		ModNetwork.sendFx(level, FxPayload.TRAIL, spell, wandOrdinal, from, to, (float) travelledBefore);
	}

	protected abstract void onEntityHit(Player owner, LivingEntity target, Vec3 hit);

	protected abstract void onBlockHit(Player owner, BlockHitResult hit, Vec3 point);

	/** Range exhausted without hitting anything: a small harmless fizzle. */
	protected void onExpire(Player owner, Vec3 point) {
		ModNetwork.sendFx(level, FxPayload.IMPACT, spell, wandOrdinal, point, Vec3.ZERO, 0.8f);
	}

	/** Point slightly outside the surface that was hit. */
	protected static Vec3 offsetFromFace(BlockHitResult hit, Vec3 point, double amount) {
		return point.add(hit.getDirection().getStepX() * amount, hit.getDirection().getStepY() * amount,
				hit.getDirection().getStepZ() * amount);
	}

	protected static Vec3 normalOf(BlockHitResult hit) {
		return new Vec3(hit.getDirection().getStepX(), hit.getDirection().getStepY(), hit.getDirection().getStepZ());
	}
}
