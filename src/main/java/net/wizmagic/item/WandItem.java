package net.wizmagic.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.wizmagic.magic.SpellManager;

/** A magic wand. Right-click casts the currently selected spell. */
public class WandItem extends Item {
	private final WandType wandType;

	public WandItem(Properties properties, WandType wandType) {
		super(properties);
		this.wandType = wandType;
	}

	public WandType getWandType() {
		return wandType;
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
			SpellManager.tryCast(serverPlayer, hand, wandType);
		}
		// CONSUME: no vanilla swing now, the wand flick happens when the spell is released.
		return InteractionResult.CONSUME;
	}
}
