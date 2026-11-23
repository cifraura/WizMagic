package net.wizmagic.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.wizmagic.WizMagic;

/** Client to server: "I want this spell selected" (spell keybinds and the spell selection screen). */
public record SelectSpellPayload(byte index) implements CustomPacketPayload {

	public static final Type<SelectSpellPayload> TYPE = new Type<>(WizMagic.id("select_spell"));

	public static final StreamCodec<RegistryFriendlyByteBuf, SelectSpellPayload> CODEC = new StreamCodec<>() {
		@Override
		public SelectSpellPayload decode(RegistryFriendlyByteBuf buf) {
			return new SelectSpellPayload(buf.readByte());
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, SelectSpellPayload p) {
			buf.writeByte(p.index);
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
