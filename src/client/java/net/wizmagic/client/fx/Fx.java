package net.wizmagic.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.client.particle.ClientParticles;
import net.wizmagic.client.particle.MagicParticle;
import net.wizmagic.config.WizConfig;

/** Small fluent helper to spawn custom particles: Fx.p("flame").at(pos).vel(v).size(0.3f).life(12).spawn(). */
public final class Fx {
	private static final RandomSource RANDOM = RandomSource.create();

	private Fx() {
	}

	public static float density() {
		return WizConfig.INSTANCE.client.particleDensity;
	}

	/** Stochastic rounding of base * density, so 0.4 particles per tick still works at low density. */
	public static int n(float base) {
		float v = base * density();
		int whole = (int) v;
		return whole + (RANDOM.nextFloat() < (v - whole) ? 1 : 0);
	}

	public static RandomSource random() {
		return RANDOM;
	}

	public static P p(String name) {
		return new P(name);
	}

	public static final class P {
		private final String name;
		private double x;
		private double y;
		private double z;
		private double vx;
		private double vy;
		private double vz;
		private float size = -1f;
		private int life = -1;
		private int color = -1;
		private int endColor = -1;
		private float alpha = 1f;
		private float gravity = Float.NaN;
		private float drag = Float.NaN;

		private P(String name) {
			this.name = name;
		}

		public P at(Vec3 v) {
			this.x = v.x;
			this.y = v.y;
			this.z = v.z;
			return this;
		}

		public P at(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
			return this;
		}

		public P vel(Vec3 v) {
			this.vx = v.x;
			this.vy = v.y;
			this.vz = v.z;
			return this;
		}

		public P vel(double x, double y, double z) {
			this.vx = x;
			this.vy = y;
			this.vz = z;
			return this;
		}

		public P size(float s) {
			this.size = s;
			return this;
		}

		public P life(int ticks) {
			this.life = ticks;
			return this;
		}

		public P color(int rgb) {
			this.color = rgb;
			return this;
		}

		public P colors(int from, int to) {
			this.color = from;
			this.endColor = to;
			return this;
		}

		public P alpha(float a) {
			this.alpha = a;
			return this;
		}

		public P gravity(float g) {
			this.gravity = g;
			return this;
		}

		public P drag(float d) {
			this.drag = d;
			return this;
		}

		public void spawn() {
			Minecraft mc = Minecraft.getInstance();
			ClientLevel level = mc.level;
			SpriteSet sprites = ClientParticles.SPRITES.get(name);
			if (level == null || sprites == null) {
				return;
			}
			MagicParticle particle = new MagicParticle(level, x, y, z, vx, vy, vz, ClientParticles.STYLES.get(name), sprites);
			particle.withSize(size).withLife(life).withAlpha(alpha);
			if (color >= 0) {
				if (endColor >= 0) {
					particle.withColors(color, endColor);
				} else {
					particle.withColor(color);
				}
			}
			if (!Float.isNaN(gravity)) {
				particle.withGravity(gravity);
			}
			if (!Float.isNaN(drag)) {
				particle.withDrag(drag);
			}
			mc.particleEngine.add(particle);
		}
	}
}
