package com.nexusreds.addons.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;

public class ModItems {

    // 1. Criamos a Tag 'upgradenexus'
    public static final TagKey<Item> NEXUS_UPGRADES = TagKey.of(RegistryKeys.ITEM, Identifier.of("nexusreds", "upgradenexus"));

    // O Blank não tem descrição, é apenas o item base
    public static final Item UPGRADE_BLANK = registerItem("upgrade_blank", new Item(new Item.Settings()));
    
    // Upgrades de Tier 1
    public static final Item RANGE_UPGRADE = registerItem("range_upgrade", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.range_upgrade").formatted(Formatting.GRAY));
        }
    });
    
    public static final Item IRRIGATION_UPGRADE = registerItem("irrigation_upgrade", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.irrigation_upgrade").formatted(Formatting.GRAY));
        }
    });

    public static final Item PLOWER_UPGRADE = registerItem("plower_upgrade", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.plower_upgrade").formatted(Formatting.GRAY));
        }
    });

    // Upgrades de Tier 2
    public static final Item RANGE_UPGRADE_TIER_2 = registerItem("range_upgrade_tier_2", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.range_upgrade_tier_2").formatted(Formatting.GRAY));
        }
    });
    
    public static final Item IRRIGATION_UPGRADE_TIER_2 = registerItem("irrigation_upgrade_tier_2", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.irrigation_upgrade_tier_2").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.nexusreds.irrigation_upgrade_tier_2.extra").formatted(Formatting.GOLD));
        }
    });

    // Upgrades de Tier 3
    public static final Item RANGE_UPGRADE_TIER_3 = registerItem("range_upgrade_tier_3", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.range_upgrade_tier_3").formatted(Formatting.GRAY));
        }
    });
    
    public static final Item IRRIGATION_UPGRADE_TIER_3 = registerItem("irrigation_upgrade_tier_3", new Item(new Item.Settings()) {
        @Override
        public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
            tooltip.add(Text.translatable("tooltip.nexusreds.irrigation_upgrade_tier_3").formatted(Formatting.GRAY));
            tooltip.add(Text.translatable("tooltip.nexusreds.irrigation_upgrade_tier_3.extra").formatted(Formatting.LIGHT_PURPLE)); // Lilás para destacar o Tier 3!
        }
    });


    // --- MÉTODO AUXILIAR DE REGISTRO ---
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("nexusreds", name), item);
    }

    // --- INICIALIZADOR ---
    public static void registerModItems() {
        System.out.println("Registrando itens do Nexus Reds Addons...");

        // Adiciona os itens à aba "Ferramentas e Utilidades" (Tools & Utilities)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(UPGRADE_BLANK);
            entries.add(RANGE_UPGRADE);
            entries.add(IRRIGATION_UPGRADE);
            entries.add(PLOWER_UPGRADE);
            entries.add(RANGE_UPGRADE_TIER_2);
            entries.add(IRRIGATION_UPGRADE_TIER_2);
            entries.add(RANGE_UPGRADE_TIER_3);
            entries.add(IRRIGATION_UPGRADE_TIER_3);
        });
    }
}