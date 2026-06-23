package com.nexusreds.addons.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    // --- REGISTRO DOS BLOCOS ---

    public static final Block SEQUENTIAL_HOPPER = registerBlock("sequential_hopper", 
        new SequentialHopperBlock(AbstractBlock.Settings.copy(Blocks.HOPPER).requiresTool()));

    public static final Block PLANTER = registerBlock("planter", 
        new PlanterBlock(AbstractBlock.Settings.copy(Blocks.DISPENSER)));

    public static final Block COPPER_PIPE_REDS = registerBlock("copper_pipe_reds", 
        new CopperPipeBlock(AbstractBlock.Settings.create()
            .strength(1.5f, 3.0f) 
            .requiresTool()       
            .nonOpaque()));

    // --- MÉTODOS AUXILIARES DE REGISTRO ---
    
    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of("nexusreds", name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, Identifier.of("nexusreds", name),
            new BlockItem(block, new Item.Settings()));
    }

    // --- INICIALIZADOR ---
    public static void registerModBlocks() {
        System.out.println("Registrando blocos do Nexus Reds Addons...");
    }
}