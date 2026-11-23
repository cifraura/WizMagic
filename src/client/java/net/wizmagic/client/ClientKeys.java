package net.wizmagic.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.wizmagic.WizMagic;
import net.wizmagic.magic.SpellType;

/** Keybinds: one per spell, plus the spell selection screen. */
public final class ClientKeys {
	public static KeyMapping[] SELECT = new KeyMapping[SpellType.VALUES.length];
	public static KeyMapping MENU;

	private ClientKeys() {
	}

	public static void register() {
		KeyMapping.Category category = KeyMapping.Category.register(WizMagic.id("main"));
		int[] defaults = {InputConstants.KEY_Z, InputConstants.KEY_X, InputConstants.KEY_C, InputConstants.KEY_V};
		for (SpellType spell : SpellType.VALUES) {
			SELECT[spell.ordinal()] = KeyBindingHelper.registerKeyBinding(new KeyMapping(
					"key.wizmagic.spell_" + spell.id, InputConstants.Type.KEYSYM, defaults[spell.ordinal()], category));
		}
		MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.wizmagic.menu", InputConstants.Type.KEYSYM, InputConstants.KEY_G, category));
	}
}
