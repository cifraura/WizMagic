package net.wizmagic.net;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.wizmagic.magic.SpellType;

public final class ModNetwork {
	/** Effects are only sent to players closer than this many blocks. */
	public static final double FX_RANGE = 112.0;

	private ModNetwork() {
	}

	public static void registerPayloads() {
		PayloadTypeRegistry.playS2C().register(FxPayload.TYPE, FxPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ManaPayload.TYPE, ManaPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(SelectSpellPayload.TYPE, SelectSpellPayload.CODEC);
	}

	/** Sends a visual effect to every nearby player. */
	public static void sendFx(ServerLevel level, byte kind, SpellType spell, Vec3 a, Vec3 b, float f) {
		sendFx(level, kind, spell, 0, a, b, f);
	}

	/** Same, also telling the clients which wand (ordinal) cast it so the wand colour can tint the effect. */
	public static void sendFx(ServerLevel level, byte kind, SpellType spell, int wandOrdinal, Vec3 a, Vec3 b, float f) {
		byte packed = (byte) (spell.ordinal() | (wandOrdinal << 4));
		FxPayload payload = new FxPayload(kind, packed, a.x, a.y, a.z, b.x, b.y, b.z, f,
				level.getRandom().nextInt());
		double range2 = FX_RANGE * FX_RANGE;
		for (ServerPlayer player : level.players()) {
			if (player.distanceToSqr(a.x, a.y, a.z) < range2 || player.distanceToSqr(b.x, b.y, b.z) < range2) {
				ServerPlayNetworking.send(player, payload);
			}
		}
	}
}
