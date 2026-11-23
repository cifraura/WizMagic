package net.wizmagic.registry;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.wizmagic.WizMagic;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * All custom particle types. None of them are vanilla particles: every name below has its own set of
 * procedurally generated textures in assets/wizmagic/textures/particle and a definition in assets/wizmagic/particles.
 */
public final class ModParticles {
	public static final String[] NAMES = {
			// wand
			"wand_glow", "wand_spark",
			// aqua
			"aqua_orb", "aqua_stream", "aqua_droplet", "aqua_spiral", "aqua_splash", "aqua_mist", "water_ring",
			// lightning
			"bolt_core", "bolt_glow", "bolt_spark", "electric_ring",
			// inferno
			"fire_orb", "flame", "ember", "fire_smoke", "fire_burst",
			// expulso
			"arcane_orb", "arcane_trail", "arcane_flash", "arcane_ring", "arcane_mote", "arcane_wave", "arcane_glow"
	};

	public static final Map<String, SimpleParticleType> TYPES = new LinkedHashMap<>();

	private ModParticles() {
	}

	public static void register() {
		for (String name : NAMES) {
			SimpleParticleType type = FabricParticleTypes.simple();
			Registry.register(BuiltInRegistries.PARTICLE_TYPE, WizMagic.id(name), type);
			TYPES.put(name, type);
		}
	}
}
