package net.wizmagic.magic;

import net.wizmagic.config.WizConfig;

/** The four spells. Colours are 0xRRGGBB and are shared by the server logic, the HUD and the client effects. */
public enum SpellType {
	AQUA("aqua", 0x2F9BFF, 0xBDEBFF),
	LIGHTNING("lightning", 0x4FA0FF, 0xFFFFFF),
	INFERNO("inferno", 0xFF6A1A, 0xFFD27A),
	EXPULSO("expulso", 0xA24BFF, 0xFFFFFF);

	public static final SpellType[] VALUES = values();

	public final String id;
	public final int primary;
	public final int secondary;

	SpellType(String id, int primary, int secondary) {
		this.id = id;
		this.primary = primary;
		this.secondary = secondary;
	}

	public String translationKey() {
		return "spell.wizmagic." + id;
	}

	public String descriptionKey() {
		return "spell.wizmagic." + id + ".desc";
	}

	public static SpellType byIndex(int index) {
		if (index < 0 || index >= VALUES.length) {
			return AQUA;
		}
		return VALUES[index];
	}

	public float manaCost() {
		WizConfig c = WizConfig.INSTANCE;
		return switch (this) {
			case AQUA -> c.aqua.manaCost;
			case LIGHTNING -> c.lightning.manaCost;
			case INFERNO -> c.inferno.manaCost;
			case EXPULSO -> c.expulso.manaCost;
		};
	}

	public int cooldownTicks() {
		WizConfig c = WizConfig.INSTANCE;
		return switch (this) {
			case AQUA -> c.aqua.cooldownTicks;
			case LIGHTNING -> c.lightning.cooldownTicks;
			case INFERNO -> c.inferno.cooldownTicks;
			case EXPULSO -> c.expulso.cooldownTicks;
		};
	}

	public int chargeTicks() {
		WizConfig c = WizConfig.INSTANCE;
		return switch (this) {
			case AQUA -> c.aqua.chargeTicks;
			case LIGHTNING -> c.lightning.chargeTicks;
			case INFERNO -> c.inferno.chargeTicks;
			case EXPULSO -> c.expulso.chargeTicks;
		};
	}
}
