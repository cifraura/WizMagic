package net.wizmagic.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.WizMagic;
import net.wizmagic.magic.SpellType;

/** Original synthesized sounds (see assets/wizmagic/sounds). Every spell has charge, cast and impact sounds. */
public final class ModSounds {
	public static final SoundEvent[] CHARGE = new SoundEvent[SpellType.VALUES.length];
	public static final SoundEvent[] CAST = new SoundEvent[SpellType.VALUES.length];
	public static final SoundEvent[] IMPACT = new SoundEvent[SpellType.VALUES.length];
	public static SoundEvent SELECT;
	public static SoundEvent NO_MANA;

	private ModSounds() {
	}

	private static SoundEvent register(String name) {
		Identifier id = WizMagic.id(name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
	}

	public static void register() {
		for (SpellType spell : SpellType.VALUES) {
			CHARGE[spell.ordinal()] = register("charge_" + spell.id);
			CAST[spell.ordinal()] = register("cast_" + spell.id);
			IMPACT[spell.ordinal()] = register("impact_" + spell.id);
		}
		SELECT = register("select");
		NO_MANA = register("no_mana");
	}

	public static void play(ServerLevel level, Vec3 pos, SoundEvent sound, float volume, float pitch) {
		level.playSound((Player) null, pos.x, pos.y, pos.z, sound, SoundSource.PLAYERS, volume, pitch);
	}
}
