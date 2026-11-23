package net.wizmagic.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.wizmagic.WizMagic;

/** Server to client: mana, selected spell and cooldowns, for the HUD. */
public record ManaPayload(float mana, float maxMana, byte selected,
						  short cd0, short cd1, short cd2, short cd3,
						  short total0, short total1, short total2, short total3) implements CustomPacketPayload {

	public static final Type<ManaPayload> TYPE = new Type<>(WizMagic.id("mana"));

	public static final StreamCodec<RegistryFriendlyByteBuf, ManaPayload> CODEC = new StreamCodec<>() {
		@Override
		public ManaPayload decode(RegistryFriendlyByteBuf buf) {
			return new ManaPayload(buf.readFloat(), buf.readFloat(), buf.readByte(),
					buf.readShort(), buf.readShort(), buf.readShort(), buf.readShort(),
					buf.readShort(), buf.readShort(), buf.readShort(), buf.readShort());
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, ManaPayload p) {
			buf.writeFloat(p.mana);
			buf.writeFloat(p.maxMana);
			buf.writeByte(p.selected);
			buf.writeShort(p.cd0);
			buf.writeShort(p.cd1);
			buf.writeShort(p.cd2);
			buf.writeShort(p.cd3);
			buf.writeShort(p.total0);
			buf.writeShort(p.total1);
			buf.writeShort(p.total2);
			buf.writeShort(p.total3);
		}
	};

	public short cooldown(int i) {
		return switch (i) {
			case 0 -> cd0;
			case 1 -> cd1;
			case 2 -> cd2;
			default -> cd3;
		};
	}

	public short total(int i) {
		return switch (i) {
			case 0 -> total0;
			case 1 -> total1;
			case 2 -> total2;
			default -> total3;
		};
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
