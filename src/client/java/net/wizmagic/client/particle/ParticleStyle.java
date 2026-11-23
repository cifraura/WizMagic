package net.wizmagic.client.particle;

/**
 * Describes how one kind of magic particle behaves: size, life, motion, colour, fading and animation.
 * Sizes are half extents in blocks (a size of 0.5 draws a quad that is one block wide).
 */
public final class ParticleStyle {
	public float sizeMin = 0.2f;
	public float sizeMax = 0.3f;
	public int lifeMin = 10;
	public int lifeMax = 16;
	/** 1.0 falls like a vanilla particle, negative values rise. */
	public float gravity = 0f;
	public float drag = 0.96f;
	public int colorA = 0xFFFFFF;
	public int colorB = 0xFFFFFF;
	/** -1 = keep the start colour for the whole life. */
	public int endColor = -1;
	public float scaleStart = 1f;
	public float scalePeak = 1f;
	public float scaleEnd = 1f;
	public float peakAt = 0.3f;
	public float alphaMax = 1f;
	public float fadeIn = 0.08f;
	public float fadeOutStart = 0.6f;
	/** Rotation speed in radians per tick (random sign). */
	public float spin = 0f;
	public float flicker = 0f;
	public boolean fullBright = true;
	/** Cycle through the sprite frames over the particle's life. */
	public boolean animate = false;
	/** Pick a random frame at birth. */
	public boolean randomFrame = false;

	public ParticleStyle size(float min, float max) {
		this.sizeMin = min;
		this.sizeMax = max;
		return this;
	}

	public ParticleStyle life(int min, int max) {
		this.lifeMin = min;
		this.lifeMax = max;
		return this;
	}

	public ParticleStyle gravity(float g, float drag) {
		this.gravity = g;
		this.drag = drag;
		return this;
	}

	public ParticleStyle color(int a, int b) {
		this.colorA = a;
		this.colorB = b;
		return this;
	}

	public ParticleStyle end(int c) {
		this.endColor = c;
		return this;
	}

	public ParticleStyle scale(float start, float peak, float end, float peakAt) {
		this.scaleStart = start;
		this.scalePeak = peak;
		this.scaleEnd = end;
		this.peakAt = peakAt;
		return this;
	}

	public ParticleStyle alpha(float max, float fadeIn, float fadeOutStart) {
		this.alphaMax = max;
		this.fadeIn = fadeIn;
		this.fadeOutStart = fadeOutStart;
		return this;
	}

	public ParticleStyle spin(float s) {
		this.spin = s;
		return this;
	}

	public ParticleStyle flicker(float f) {
		this.flicker = f;
		return this;
	}

	public ParticleStyle animated() {
		this.animate = true;
		return this;
	}

	public ParticleStyle randomFrame() {
		this.randomFrame = true;
		return this;
	}

	public ParticleStyle dim() {
		this.fullBright = false;
		return this;
	}
}
