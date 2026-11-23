package net.wizmagic.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemLore;
import net.wizmagic.WizMagic;
import net.wizmagic.item.WandItem;
import net.wizmagic.item.WandType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModItems {
	public static final Map<WandType, Item> WANDS = new EnumMap<>(WandType.class);

	private ModItems() {
	}

	public static void register() {
		for (WandType type : WandType.VALUES) {
			ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WizMagic.id(type.itemName()));
			ItemLore lore = new ItemLore(List.of(
					Component.translatable("tooltip.wizmagic.wood", Component.translatable(type.woodKey)).withStyle(ChatFormatting.GOLD),
					Component.translatable("tooltip.wizmagic.core", Component.translatable(type.coreKey)).withStyle(ChatFormatting.AQUA),
					Component.translatable("tooltip.wizmagic.hint").withStyle(ChatFormatting.DARK_GRAY)));
			Item.Properties properties = new Item.Properties()
					.setId(key)
					.stacksTo(1)
					.component(DataComponents.LORE, lore);
			Item item = Registry.register(BuiltInRegistries.ITEM, key, new WandItem(properties, type));
			WANDS.put(type, item);
		}

		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
			for (WandType type : WandType.VALUES) {
				entries.accept(WANDS.get(type));
			}
		});
		ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.COMBAT).register(entries -> {
			for (WandType type : WandType.VALUES) {
				entries.accept(WANDS.get(type));
			}
		});
	}
}
