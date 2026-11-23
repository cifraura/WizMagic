package net.wizmagic.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;

/**
 * The single particle class used by every custom magic effect. Behaviour comes from a {@link ParticleStyle},
 * the look comes from the sprite frames of the particle type. It always renders translucent and fully lit
 * (glowing), never uses vanilla particle sprites and never collides with blocks.
 */
public class MagicParticle extends SingleQuadParticle {
	private final ParticleStyle style;
	private final SpriteSet sprites;

	private float baseSize;
	private float sizeMul = 1f;
	private float alphaMul = 1f;
	private float r0;
	private float g0;
	private float b0;
	private float r1;
	private float g1;
	private float b1;
	private boolean hasEnd;
	private final float spinVel;
	private final float flickerPhase;
	private float gravityValue;
	private float dragValue;

	public MagicParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz,
						 ParticleStyle style, SpriteSet sprites) {
		super(level, x, y, z, sprites.first());
		this.style = style;
		this.sprites = sprites;

		this.xd = vx;
		this.yd = vy;
		this.zd = vz;
		this.hasPhysics = false;

		this.lifetime = style.lifeMin + (style.lifeMax > style.lifeMin ? this.random.nextInt(style.lifeMax - style.lifeMin + 1) : 0);
		this.baseSize = Mth.lerp(this.random.nextFloat(), style.sizeMin, style.sizeMax);
		this.gravityValue = style.gravity;
		this.dragValue = style.drag;

		int start = lerpColor(style.colorA, style.colorB, this.random.nextFloat());
		setStartColor(start);
		if (style.endColor >= 0) {
			setEndColor(style.endColor);
		}

		this.roll = this.random.nextFloat() * Mth.TWO_PI;
		this.oRoll = this.roll;
		this.spinVel = style.spin * (this.random.nextBoolean() ? 1f : -1f) * (0.5f + this.random.nextFloat());
		this.flickerPhase = this.random.nextFloat() * 10f;

		if (style.animate) {
			setSpriteFromAge(sprites);
		} else if (style.randomFrame) {
			setSprite(sprites.get(this.random));
		}
		this.alpha = 0f;
		setColor(r0, g0, b0);
	}

	// ---- builder style tweaks used right after construction -----------------------------------------

	public MagicParticle withSize(float absoluteHalfExtent) {
		if (absoluteHalfExtent > 0f) {
			this.baseSize = absoluteHalfExtent;
		}
		return this;
	}

	public MagicParticle withSizeMul(float mul) {
		this.sizeMul *= mul;
		return this;
	}

	public MagicParticle withLife(int ticks) {
		if (ticks > 0) {
			this.lifetime = ticks;
		}
		return this;
	}

	public MagicParticle withLifeMul(float mul) {
		this.lifetime = Math.max(2, Math.round(this.lifetime * mul));
		return this;
	}

	public MagicParticle withColor(int rgb) {
		setStartColor(rgb);
		this.hasEnd = false;
		setColor(r0, g0, b0);
		return this;
	}

	public MagicParticle withColors(int startRgb, int endRgb) {
		setStartColor(startRgb);
		setEndColor(endRgb);
		setColor(r0, g0, b0);
		return this;
	}

	public MagicParticle withAlpha(float mul) {
		this.alphaMul = mul;
		return this;
	}

	public MagicParticle withGravity(float g) {
		this.gravityValue = g;
		return this;
	}

	public MagicParticle withDrag(float d) {
		this.dragValue = d;
		return this;
	}

	// ---- helpers -----------------------------------------------------------------------------------

	private void setStartColor(int rgb) {
		this.r0 = ((rgb >> 16) & 0xFF) / 255f;
		this.g0 = ((rgb >> 8) & 0xFF) / 255f;
		this.b0 = (rgb & 0xFF) / 255f;
	}

	private void setEndColor(int rgb) {
		this.r1 = ((rgb >> 16) & 0xFF) / 255f;
		this.g1 = ((rgb >> 8) & 0xFF) / 255f;
		this.b1 = (rgb & 0xFF) / 255f;
		this.hasEnd = true;
	}

	public static int lerpColor(int a, int b, float t) {
		int r = (int) Mth.lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
		int g = (int) Mth.lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
		int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
		return (r << 16) | (g << 8) | bl;
	}

	private float scaleAt(float t) {
		float peak = Math.max(0.001f, style.peakAt);
		if (t < peak) {
			return Mth.lerp(t / peak, style.scaleStart, style.scalePeak);
		}
		float rest = Math.max(0.001f, 1f - peak);
		return Mth.lerp(Math.min(1f, (t - peak) / rest), style.scalePeak, style.scaleEnd);
	}

	// ---- Particle overrides ------------------------------------------------------------------------

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.oRoll = this.roll;

		if (this.age++ >= this.lifetime) {
			this.remove();
			return;
		}

		this.yd -= 0.04 * this.gravityValue;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= this.dragValue;
		this.yd *= this.dragValue;
		this.zd *= this.dragValue;
		this.roll += this.spinVel;

		float t = (float) this.age / (float) this.lifetime;
		if (this.hasEnd) {
			setColor(Mth.lerp(t, r0, r1), Mth.lerp(t, g0, g1), Mth.lerp(t, b0, b1));
		}

		float a = style.alphaMax * alphaMul;
		if (style.fadeIn > 0f && t < style.fadeIn) {
			a *= t / style.fadeIn;
		} else if (t > style.fadeOutStart) {
			a *= Math.max(0f, 1f - (t - style.fadeOutStart) / Math.max(0.001f, 1f - style.fadeOutStart));
		}
		if (style.flicker > 0f) {
			a *= 1f - style.flicker * (0.5f + 0.5f * Mth.sin((age + flickerPhase) * 2.1f));
		}
		setAlpha(Mth.clamp(a, 0f, 1f));

		if (style.animate) {
			setSpriteFromAge(sprites);
		}
	}

	@Override
	public float getQuadSize(float partialTick) {
		float t = Mth.clamp(((float) this.age + partialTick) / (float) this.lifetime, 0f, 1f);
		float size = baseSize * sizeMul * scaleAt(t);
		if (style.flicker > 0f) {
			size *= 1f + 0.12f * style.flicker * Mth.sin((age + partialTick + flickerPhase) * 1.7f);
		}
		return size;
	}

	@Override
	protected int getLightColor(float partialTick) {
		return style.fullBright ? 0xF000F0 : super.getLightColor(partialTick);
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}
}
