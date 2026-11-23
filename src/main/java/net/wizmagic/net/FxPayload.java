package net.wizmagic.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.wizmagic.WizMagic;

/**
 * A compact "play this visual effect" message (server to client). The client generates all the particles
 * from it, so the server never has to send hundreds of particle packets.
 *
 * <p>kind: what to play. spell: {@link net.wizmagic.magic.SpellType} ordinal. a/b: two positions
 * whose meaning depends on the kind. f: kind specific number. seed: random seed so that everybody sees the same shape.
 */
public record FxPayload(byte kind, byte spell, double ax, double ay, double az,
						double bx, double by, double bz, float f, int seed) implements CustomPacketPayload {

	/** a = wand tip, b = aim direction, f = charge progress 0..1. */
	public static final byte CHARGE = 0;
	/** a = wand tip, b = aim direction. The moment the spell leaves the wand. */
	public static final byte RELEASE = 1;
	/** a = segment start, b = segment end, f = distance travelled before this segment. */
	public static final byte TRAIL = 2;
	/** a = impact point, b = surface normal (or zero), f = radius. */
	public static final byte IMPACT = 3;
	/** a = start, b = end, f = 0 for the main beam, 1 for a chained arc. */
	public static final byte ARC = 4;
	/** a = entity feet, b = (width, height, 0), f = ticks left. */
	public static final byte AURA = 5;

	public static final Type<FxPayload> TYPE = new Type<>(WizMagic.id("fx"));

	public static final StreamCodec<RegistryFriendlyByteBuf, FxPayload> CODEC = new StreamCodec<>() {
		@Override
		public FxPayload decode(RegistryFriendlyByteBuf buf) {
			byte kind = buf.readByte();
			byte spell = buf.readByte();
			double ax = buf.readDouble();
			double ay = buf.readDouble();
			double az = buf.readDouble();
			double bx = buf.readDouble();
			double by = buf.readDouble();
			double bz = buf.readDouble();
			float f = buf.readFloat();
			int seed = buf.readInt();
			return new FxPayload(kind, spell, ax, ay, az, bx, by, bz, f, seed);
		}

		@Override
		public void encode(RegistryFriendlyByteBuf buf, FxPayload p) {
			buf.writeByte(p.kind);
			buf.writeByte(p.spell);
			buf.writeDouble(p.ax);
			buf.writeDouble(p.ay);
			buf.writeDouble(p.az);
			buf.writeDouble(p.bx);
			buf.writeDouble(p.by);
			buf.writeDouble(p.bz);
			buf.writeFloat(p.f);
			buf.writeInt(p.seed);
		}
	};

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
