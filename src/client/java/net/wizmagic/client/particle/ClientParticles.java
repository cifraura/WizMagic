package net.wizmagic.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.wizmagic.registry.ModParticles;

import java.util.HashMap;
import java.util.Map;

/** Registers a provider for every custom particle type and keeps their sprite sets for direct spawning. */
public final class ClientParticles {
	public static final Map<String, SpriteSet> SPRITES = new HashMap<>();
	public static final Map<String, ParticleStyle> STYLES = new HashMap<>();

	private ClientParticles() {
	}

	public static void register() {
		for (Map.Entry<String, SimpleParticleType> entry : ModParticles.TYPES.entrySet()) {
			final String name = entry.getKey();
			final ParticleStyle style = ParticleStyles.styleFor(name);
			STYLES.put(name, style);
			ParticleFactoryRegistry.getInstance().register(entry.getValue(), provider -> {
				SPRITES.put(name, provider);
				return (type, level, x, y, z, vx, vy, vz, random) ->
						new MagicParticle(level, x, y, z, vx, vy, vz, style, provider);
			});
		}
	}
}
