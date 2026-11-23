package net.wizmagic.client.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.wizmagic.WizMagic;
import net.wizmagic.client.ClientKeys;
import net.wizmagic.client.ClientMagicState;
import net.wizmagic.item.WandItem;
import net.wizmagic.magic.SpellType;

/** HUD: the four spells, the selected one highlighted, cooldown overlays and the mana bar. */
public final class SpellHud {
	public static final Identifier[] ICONS = new Identifier[SpellType.VALUES.length];

	static {
		for (SpellType s : SpellType.VALUES) {
			ICONS[s.ordinal()] = WizMagic.id("textures/gui/spell_" + s.id + ".png");
		}
	}

	private SpellHud() {
	}

	private static int argb(int rgb) {
		return 0xFF000000 | rgb;
	}

	public static void render(GuiGraphics g, DeltaTracker delta) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.options.hideGui) {
			return;
		}
		boolean holding = player.getMainHandItem().getItem() instanceof WandItem
				|| player.getOffhandItem().getItem() instanceof WandItem;
		if (!holding) {
			return;
		}

		int panelW = 128;
		int panelH = 64;
		int x = g.guiWidth() - panelW - 8;
		int y = g.guiHeight() - panelH - 8;
		SpellType selected = SpellType.byIndex(ClientMagicState.selected);
		int accent = argb(selected.primary);

		g.fill(x, y, x + panelW, y + panelH, 0xB0100818);
		g.fill(x, y, x + panelW, y + 1, accent);
		g.fill(x, y + panelH - 1, x + panelW, y + panelH, accent);
		g.fill(x, y, x + 1, y + panelH, accent);
		g.fill(x + panelW - 1, y, x + panelW, y + panelH, accent);

		for (SpellType spell : SpellType.VALUES) {
			int i = spell.ordinal();
			int sx = x + 6 + i * 30;
			int sy = y + 6;
			boolean sel = spell == selected;
			g.fill(sx, sy, sx + 24, sy + 24, sel ? 0xFF2A1A44 : 0xFF140C20);
			if (sel) {
				int c = argb(spell.primary);
				g.fill(sx - 1, sy - 1, sx + 25, sy, c);
				g.fill(sx - 1, sy + 24, sx + 25, sy + 25, c);
				g.fill(sx - 1, sy, sx, sy + 24, c);
				g.fill(sx + 24, sy, sx + 25, sy + 24, c);
			}
			g.blit(RenderPipelines.GUI_TEXTURED, ICONS[i], sx + 4, sy + 4, 0f, 0f, 16, 16, 16, 16);

			float frac = ClientMagicState.COOLDOWN[i] / ClientMagicState.COOLDOWN_TOTAL[i];
			if (frac > 0f) {
				int hh = Math.min(24, Math.round(24 * frac));
				g.fill(sx, sy + 24 - hh, sx + 24, sy + 24, 0xB0000000);
			}
			if (ClientKeys.SELECT[i] != null) {
				g.drawString(mc.font, ClientKeys.SELECT[i].getTranslatedKeyMessage(), sx + 2, sy + 26, sel ? 0xFFFFFFFF : 0xFF8F86A0, true);
			}
		}

		g.drawString(mc.font, Component.translatable(selected.translationKey()), x + 6, y + 44, accent, true);

		int bx = x + 6;
		int by = y + panelH - 10;
		int bw = panelW - 12;
		float ratio = ClientMagicState.maxMana <= 0 ? 0 : Math.max(0f, Math.min(1f, ClientMagicState.mana / ClientMagicState.maxMana));
		g.fill(bx - 1, by - 1, bx + bw + 1, by + 6, 0xFF000000);
		g.fill(bx, by, bx + bw, by + 5, 0xFF1A2440);
		int fillW = Math.round(bw * ratio);
		g.fill(bx, by, bx + fillW, by + 5, 0xFF2F8BFF);
		g.fill(bx, by, bx + fillW, by + 2, 0xFF8FD0FF);
		String text = Math.round(ClientMagicState.mana) + "/" + Math.round(ClientMagicState.maxMana);
		g.drawString(mc.font, text, x + panelW - 6 - mc.font.width(text), y + 44, 0xFFBFD8FF, true);
	}
}
