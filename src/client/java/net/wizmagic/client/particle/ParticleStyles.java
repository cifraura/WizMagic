package net.wizmagic.client.particle;

/** The behaviour table of every custom particle type (names match ModParticles.NAMES). */
public final class ParticleStyles {
	private ParticleStyles() {
	}

	public static ParticleStyle styleFor(String name) {
		return switch (name) {
			case "wand_glow" -> new ParticleStyle().size(0.10f, 0.16f).life(14, 22).gravity(0f, 0.9f)
					.scale(0.6f, 1f, 0.2f, 0.3f).alpha(0.85f, 0.15f, 0.5f).flicker(0.3f);
			case "wand_spark" -> new ParticleStyle().size(0.06f, 0.10f).life(10, 18).gravity(-0.25f, 0.9f)
					.scale(1f, 1f, 0f, 0.3f).alpha(1f, 0.05f, 0.5f).spin(0.15f).flicker(0.4f);

			case "aqua_orb" -> new ParticleStyle().size(0.2f, 0.2f).life(3, 3).alpha(0.95f, 0f, 0.6f).flicker(0.15f).spin(0.05f);
			case "aqua_stream" -> new ParticleStyle().size(0.22f, 0.30f).life(7, 10).gravity(0.12f, 0.92f)
					.color(0xBFE9FF, 0x7CC8FF).end(0x3F9BFF).scale(0.7f, 1.15f, 1.5f, 0.3f)
					.alpha(0.75f, 0.1f, 0.5f).animated().spin(0.05f);
			case "aqua_droplet" -> new ParticleStyle().size(0.06f, 0.10f).life(16, 26).gravity(1.0f, 0.98f)
					.scale(1f, 1f, 0.8f, 0.5f).alpha(0.9f, 0.02f, 0.8f);
			case "aqua_spiral" -> new ParticleStyle().size(0.08f, 0.11f).life(6, 9).gravity(0f, 0.9f)
					.color(0x9FDCFF, 0xFFFFFF).scale(1f, 0.9f, 0.1f, 0.3f).alpha(1f, 0f, 0.4f);
			case "aqua_splash" -> new ParticleStyle().size(0.6f, 0.7f).life(9, 11).animated()
					.scale(0.5f, 1f, 1.2f, 0.3f).alpha(0.9f, 0.05f, 0.5f);
			case "aqua_mist" -> new ParticleStyle().size(0.35f, 0.50f).life(16, 26).gravity(-0.01f, 0.94f)
					.scale(0.5f, 1f, 1.6f, 0.5f).alpha(0.35f, 0.2f, 0.5f).spin(0.02f);
			case "water_ring" -> new ParticleStyle().size(0.3f, 0.3f).life(10, 10).scale(0.15f, 1f, 1f, 1f)
					.alpha(0.8f, 0.02f, 0.5f);

			case "bolt_core" -> new ParticleStyle().size(0.10f, 0.14f).life(3, 4).color(0xFFFFFF, 0xCFE6FF)
					.scale(1f, 1f, 0.5f, 0.3f).alpha(1f, 0f, 0.5f).flicker(0.25f);
			case "bolt_glow" -> new ParticleStyle().size(0.30f, 0.40f).life(4, 5).color(0x4FA0FF, 0x9FD0FF)
					.scale(1f, 1.2f, 0.8f, 0.3f).alpha(0.55f, 0f, 0.4f).flicker(0.2f);
			case "bolt_spark" -> new ParticleStyle().size(0.08f, 0.12f).life(6, 12).gravity(0.05f, 0.85f)
					.color(0xFFFFFF, 0x7FB8FF).scale(1f, 1f, 0f, 0.3f).alpha(1f, 0f, 0.5f).spin(0.2f).flicker(0.3f).animated();
			case "electric_ring" -> new ParticleStyle().size(0.3f, 0.3f).life(9, 9).scale(0.2f, 1f, 1f, 1f)
					.alpha(0.9f, 0.02f, 0.5f).spin(0.08f);

			case "fire_orb" -> new ParticleStyle().size(0.25f, 0.25f).life(3, 3).color(0xFFB347, 0xFF8A2B)
					.alpha(0.95f, 0f, 0.6f).flicker(0.12f);
			case "flame" -> new ParticleStyle().size(0.22f, 0.36f).life(12, 20).gravity(-0.2f, 0.93f).animated()
					.scale(0.6f, 1.1f, 0.2f, 0.25f).alpha(0.95f, 0.05f, 0.5f);
			case "ember" -> new ParticleStyle().size(0.035f, 0.06f).life(24, 44).gravity(0.05f, 0.95f)
					.color(0xFFD27A, 0xFFB347).end(0xFF4A12).scale(1f, 1f, 0.5f, 0.4f).alpha(1f, 0f, 0.6f).flicker(0.5f);
			case "fire_smoke" -> new ParticleStyle().size(0.35f, 0.55f).life(26, 40).gravity(-0.04f, 0.94f)
					.color(0xFF9A3A, 0xFF7A2A).end(0x3A2A25).scale(0.5f, 1f, 1.8f, 0.6f)
					.alpha(0.5f, 0.15f, 0.6f).spin(0.03f);
			case "fire_burst" -> new ParticleStyle().size(0.8f, 0.8f).life(9, 9).animated()
					.scale(0.4f, 1f, 1.25f, 0.4f).alpha(0.95f, 0.02f, 0.5f);

			case "arcane_orb" -> new ParticleStyle().size(0.25f, 0.25f).life(3, 3).color(0xFFFFFF, 0xD9B8FF)
					.alpha(0.95f, 0f, 0.6f).flicker(0.1f).spin(0.1f);
			case "arcane_trail" -> new ParticleStyle().size(0.30f, 0.40f).life(8, 11).animated().spin(0.12f)
					.color(0xD6A8FF, 0xB070FF).end(0x6B2CC0).scale(0.8f, 1f, 0.2f, 0.2f).alpha(0.7f, 0.05f, 0.6f);
			case "arcane_flash" -> new ParticleStyle().size(1f, 1f).life(5, 5).scale(0.6f, 1.1f, 0.9f, 0.3f)
					.alpha(1f, 0f, 0.7f);
			case "arcane_ring" -> new ParticleStyle().size(0.3f, 0.3f).life(12, 12).scale(0.15f, 1f, 1f, 1f)
					.color(0xC58BFF, 0xE5C8FF).end(0x7B3FE0).alpha(0.9f, 0.02f, 0.55f).spin(0.03f);
			case "arcane_mote" -> new ParticleStyle().size(0.07f, 0.12f).life(20, 50).gravity(0.01f, 0.9f)
					.color(0xE5C8FF, 0xA24BFF).end(0x5A2AA0).scale(1f, 1f, 0f, 0.4f).alpha(1f, 0.02f, 0.6f).spin(0.1f).flicker(0.25f);
			case "arcane_wave" -> new ParticleStyle().size(1f, 1f).life(14, 14).scale(0.1f, 1f, 1f, 1f)
					.alpha(0.5f, 0.05f, 0.6f);
			case "arcane_glow" -> new ParticleStyle().size(0.2f, 0.5f).life(30, 45).gravity(0f, 0.93f)
					.color(0xB070FF, 0xD9B8FF).end(0x6030B0).scale(0.6f, 1f, 0.3f, 0.2f).alpha(0.55f, 0.15f, 0.6f);
			default -> new ParticleStyle();
		};
	}
}
