package net.wizmagic.item;

/**
 * The six wands. Every wand has its own wood, magical core and glow colour. The glow colour tints the
 * sparkles around the wand tip and the casting charge, so each wand looks different when it casts.
 */
public enum WandType {
	HOLLY_PHOENIX("holly_phoenix", "wood.holly", "core.phoenix", 0xFF8A2B),
	ELDER_THESTRAL("elder_thestral", "wood.elder", "core.thestral", 0xDCE4FF),
	YEW_DRAGON("yew_dragon", "wood.yew", "core.dragon", 0x35E07D),
	WILLOW_UNICORN("willow_unicorn", "wood.willow", "core.unicorn", 0xA8E8FF),
	EBONY_THUNDERBIRD("ebony_thunderbird", "wood.ebony", "core.thunderbird", 0x6FA8FF),
	VINE_VEELA("vine_veela", "wood.vine", "core.veela", 0xE08BFF);

	public static final WandType[] VALUES = values();

	public final String id;
	public final String woodKey;
	public final String coreKey;
	/** 0xRRGGBB glow colour. */
	public final int glow;

	WandType(String id, String woodKey, String coreKey, int glow) {
		this.id = id;
		this.woodKey = "wand.wizmagic." + woodKey;
		this.coreKey = "wand.wizmagic." + coreKey;
		this.glow = glow;
	}

	public String itemName() {
		return "wand_" + id;
	}
}
