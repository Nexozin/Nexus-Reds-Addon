package com.nexusreds.addons.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModItems {

    // 1. Criamos a Tag 'upgradenexus'
    public static final TagKey<Item> NEXUS_UPGRADES = TagKey.of(RegistryKeys.ITEM, Identifier.of("nexusreds", "upgradenexus"));

    public static final Item UPGRADE_BLANK = registerItem("upgrade_blank", new Item(new Item.Settings()));
    public static final Item RANGE_UPGRADE = registerItem("range_upgrade", new Item(new Item.Settings()));
    public static final Item IRRIGATION_UPGRADE = registerItem("irrigation_upgrade", new Item(new Item.Settings()));


    // --- MÉTODO AUXILIAR DE REGISTRO ---
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of("nexusreds", name), item);
    }

    // --- INICIALIZADOR ---
    public static void registerModItems() {
        System.out.println("Registrando itens do Nexus Reds Addons...");

        // Adiciona o item à aba "Ferramentas e Utilidades" (Tools & Utilities)
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(RANGE_UPGRADE);
        });
        
        // Se quiser que apareça também na aba de Redstone, basta descomentar as linhas abaixo:
        /*
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE_BLOCKS).register(entries -> {
            entries.add(RANGE_UPGRADE);
        });
        */
    }
}