package net.wizmagic.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Geometry helpers shared by the server logic and the client effects. */
public final class WandMath {
	private WandMath() {
	}

	/** Approximate position of the wand tip: in front of the eyes, to the side of the holding hand. */
	public static Vec3 tip(Player player, InteractionHand hand) {
		Vec3 look = player.getLookAngle();
		Vec3 right = look.cross(new Vec3(0, 1, 0));
		if (right.lengthSqr() < 1.0E-4) {
			right = new Vec3(1, 0, 0);
		} else {
			right = right.normalize();
		}
		boolean rightHand = (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);
		double side = rightHand ? 0.34 : -0.34;
		return player.getEyePosition().add(look.scale(0.85)).add(right.scale(side)).add(0, -0.30, 0);
	}

	/** A vector perpendicular to the given direction (any, stable). */
	public static Vec3 perpendicular(Vec3 dir) {
		Vec3 ref = Math.abs(dir.y) > 0.95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
		Vec3 p = dir.cross(ref);
		if (p.lengthSqr() < 1.0E-6) {
			return new Vec3(1, 0, 0);
		}
		return p.normalize();
	}
}
