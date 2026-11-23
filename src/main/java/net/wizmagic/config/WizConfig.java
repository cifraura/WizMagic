package net.wizmagic.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.wizmagic.WizMagic;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JSON configuration (config/wizmagic.json). Every gameplay number of the four spells is configurable.
 * The file is created with defaults on first run.
 */
public class WizConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	public static WizConfig INSTANCE = new WizConfig();

	public static class General {
		public float maxMana = 100f;
		public float manaRegenPerSecond = 5f;
		public int regenDelayTicks = 30;
		public boolean creativeIgnoresCosts = true;
	}

	public static class Client {
		/** 0.25 = minimal particles, 1.0 = default, 2.0 = cinematic. */
		public float particleDensity = 1.0f;
		public boolean wandAmbientGlow = true;
	}

	public static class Aqua {
		public float manaCost = 12f;
		public int cooldownTicks = 24;
		public int chargeTicks = 6;
		public float damage = 5f;
		public float range = 40f;
		public float speed = 1.5f;
		public float knockback = 1.6f;
		public int slowTicks = 80;
		public int slowAmplifier = 1;
		public float splashRadius = 3.0f;
	}

	public static class Lightning {
		public float manaCost = 18f;
		public int cooldownTicks = 30;
		public int chargeTicks = 8;
		public float damage = 8f;
		public float range = 45f;
		public float speed = 4.0f;
		public int stunTicks = 14;
		public int chainTargets = 3;
		public float chainRadius = 6f;
		public float chainDamageFactor = 0.6f;
		public int auraTicks = 40;
	}

	public static class Inferno {
		public float manaCost = 20f;
		public int cooldownTicks = 36;
		public int chargeTicks = 10;
		public float damage = 7f;
		public float range = 40f;
		public float speed = 1.2f;
		public int burnSeconds = 6;
		public float blastRadius = 3.5f;
		public boolean igniteBlocks = true;
	}

	public static class Expulso {
		public float manaCost = 30f;
		public int cooldownTicks = 60;
		public int chargeTicks = 12;
		public float damage = 14f;
		public float range = 50f;
		public float speed = 2.4f;
		public float explosionRadius = 4.5f;
		public float knockback = 2.2f;
		public boolean destroyBlocks = true;
		public boolean dropBlocks = true;
		/** Blocks harder than this are never destroyed (stone = 1.5, obsidian = 50). */
		public float maxBlockHardness = 3.0f;
		/** Blocks that can never be destroyed. */
		public List<String> blockBlacklist = new ArrayList<>(Arrays.asList(
				"minecraft:bedrock", "minecraft:obsidian", "minecraft:crying_obsidian",
				"minecraft:reinforced_deepslate", "minecraft:end_portal_frame", "minecraft:barrier",
				"minecraft:spawner", "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest",
				"minecraft:barrel", "minecraft:beacon"));
		/** If not empty, ONLY these blocks can be destroyed. */
		public List<String> blockWhitelist = new ArrayList<>();
	}

	public General general = new General();
	public Client client = new Client();
	public Aqua aqua = new Aqua();
	public Lightning lightning = new Lightning();
	public Inferno inferno = new Inferno();
	public Expulso expulso = new Expulso();

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("wizmagic.json");
	}

	public static void load() {
		Path file = path();
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				WizConfig loaded = GSON.fromJson(reader, WizConfig.class);
				if (loaded != null) {
					INSTANCE = loaded;
				}
			} catch (Exception e) {
				WizMagic.LOGGER.error("Could not read wizmagic.json, using defaults", e);
			}
		}
		INSTANCE.sanitize();
		save(); // rewrites the file so that new options appear after updates
	}

	public static void save() {
		try {
			Files.createDirectories(path().getParent());
			try (Writer writer = Files.newBufferedWriter(path())) {
				GSON.toJson(INSTANCE, writer);
			}
		} catch (IOException e) {
			WizMagic.LOGGER.error("Could not write wizmagic.json", e);
		}
	}

	private void sanitize() {
		if (general == null) general = new General();
		if (client == null) client = new Client();
		if (aqua == null) aqua = new Aqua();
		if (lightning == null) lightning = new Lightning();
		if (inferno == null) inferno = new Inferno();
		if (expulso == null) expulso = new Expulso();
		if (expulso.blockBlacklist == null) expulso.blockBlacklist = new ArrayList<>();
		if (expulso.blockWhitelist == null) expulso.blockWhitelist = new ArrayList<>();

		general.maxMana = Math.max(1f, general.maxMana);
		general.manaRegenPerSecond = Math.max(0f, general.manaRegenPerSecond);
		client.particleDensity = Math.max(0.1f, Math.min(3f, client.particleDensity));

		aqua.speed = clampSpeed(aqua.speed);
		lightning.speed = clampSpeed(lightning.speed);
		inferno.speed = clampSpeed(inferno.speed);
		expulso.speed = clampSpeed(expulso.speed);
		aqua.range = clampRange(aqua.range);
		lightning.range = clampRange(lightning.range);
		inferno.range = clampRange(inferno.range);
		expulso.range = clampRange(expulso.range);
		expulso.explosionRadius = Math.max(1f, Math.min(16f, expulso.explosionRadius));
		inferno.blastRadius = Math.max(1f, Math.min(12f, inferno.blastRadius));
		aqua.splashRadius = Math.max(1f, Math.min(10f, aqua.splashRadius));
		aqua.chargeTicks = Math.max(1, aqua.chargeTicks);
		lightning.chargeTicks = Math.max(1, lightning.chargeTicks);
		inferno.chargeTicks = Math.max(1, inferno.chargeTicks);
		expulso.chargeTicks = Math.max(1, expulso.chargeTicks);
	}

	private static float clampSpeed(float s) {
		return Math.max(0.3f, Math.min(6f, s));
	}

	private static float clampRange(float r) {
		return Math.max(4f, Math.min(128f, r));
	}
}
