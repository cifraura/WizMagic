package net.wizmagic.client.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.wizmagic.client.ClientMagicState;
import net.wizmagic.magic.SpellType;
import net.wizmagic.net.SelectSpellPayload;

/** The spell selection interface: pick a spell with the mouse. */
public class SpellSelectScreen extends Screen {
	private static final int ROW_H = 34;
	private static final int BTN_W = 190;

	public SpellSelectScreen() {
		super(Component.translatable("screen.wizmagic.spells"));
	}

	private int top() {
		return this.height / 2 - (SpellType.VALUES.length * ROW_H) / 2 + 6;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - BTN_W / 2 + 14;
		for (SpellType spell : SpellType.VALUES) {
			int y = top() + spell.ordinal() * ROW_H;
			final int index = spell.ordinal();
			this.addRenderableWidget(Button.builder(Component.translatable(spell.translationKey()), button -> {
				ClientPlayNetworking.send(new SelectSpellPayload((byte) index));
				ClientMagicState.selectLocally(index);
				this.onClose();
			}).bounds(left, y, BTN_W, 20).build());
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
		int left = this.width / 2 - BTN_W / 2 + 14;
		g.fill(left - 52, top() - 36, left + BTN_W + 16, top() + SpellType.VALUES.length * ROW_H + 2, 0xC0100818);
		g.drawCenteredString(this.font, this.title, this.width / 2 + 14, top() - 26, 0xFFE5C8FF);

		for (SpellType spell : SpellType.VALUES) {
			int y = top() + spell.ordinal() * ROW_H;
			boolean sel = spell.ordinal() == ClientMagicState.selected;
			if (sel) {
				g.fill(left - 42, y - 3, left + BTN_W + 8, y + 31, 0x40000000 | spell.primary);
			}
			g.blit(RenderPipelines.GUI_TEXTURED, SpellHud.ICONS[spell.ordinal()], left - 34, y + 1, 0f, 0f, 16, 16, 16, 16);
			g.drawString(this.font, Component.translatable(spell.descriptionKey()), left + 2, y + 22, 0xFF9C94B0, false);
		}
		super.render(g, mouseX, mouseY, partialTick);
	}
}
