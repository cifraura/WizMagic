package net.wizmagic;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.wizmagic.config.WizConfig;
import net.wizmagic.magic.SpellManager;
import net.wizmagic.net.ModNetwork;
import net.wizmagic.registry.ModItems;
import net.wizmagic.registry.ModParticles;
import net.wizmagic.registry.ModSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Common (server + client) entrypoint. */
public class WizMagic implements ModInitializer {
	public static final String MOD_ID = "wizmagic";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		WizConfig.load();
		ModSounds.register();
		ModParticles.register();
		ModItems.register();
		ModNetwork.registerPayloads();
		SpellManager.init();
		LOGGER.info("Wizarding Magic loaded: 6 wands, 4 spells, custom particles.");
	}
}
