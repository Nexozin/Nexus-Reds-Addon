package com.nexusreds.addons;

import com.nexusreds.addons.block.PlanterBlock;
import com.nexusreds.addons.block.SequentialHopperBlock;
import com.nexusreds.addons.block.entity.PlanterBlockEntity;
import com.nexusreds.addons.block.entity.SequentialHopperBlockEntity;
import com.nexusreds.addons.item.ModItems;
import com.nexusreds.addons.screen.PlanterScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NexusRedsMod implements ModInitializer {
    public static final String MOD_ID = "nexusreds"; 

    public static final Block SEQUENTIAL_HOPPER = new SequentialHopperBlock(
        AbstractBlock.Settings.copy(Blocks.HOPPER).requiresTool()
    );

    public static final Block PLANTER = new PlanterBlock(
        AbstractBlock.Settings.copy(Blocks.DISPENSER)
    );

    public static final Item NEXUS_WRENCH = new com.nexusreds.addons.item.NexusWrenchItem(new Item.Settings().maxCount(1));

    public static BlockEntityType<SequentialHopperBlockEntity> SEQUENTIAL_HOPPER_ENTITY;
    public static BlockEntityType<PlanterBlockEntity> PLANTER_ENTITY;
    public static ScreenHandlerType<PlanterScreenHandler> PLANTER_SCREEN_HANDLER;

    public static final ItemGroup NEXUSREDS_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(SEQUENTIAL_HOPPER))
            .displayName(Text.translatable("itemGroup.nexusreds.main_group"))
            .entries((context, entries) -> {
                entries.add(NEXUS_WRENCH);
                entries.add(SEQUENTIAL_HOPPER);
                entries.add(PLANTER);
                entries.add(com.nexusreds.addons.item.ModItems.UPGRADE_BLANK);
                entries.add(com.nexusreds.addons.item.ModItems.RANGE_UPGRADE);
                entries.add(ModItems.IRRIGATION_UPGRADE);
                entries.add(ModItems.PLOWER_UPGRADE);
                entries.add(ModItems.RANGE_UPGRADE_TIER_2);
                entries.add(ModItems.IRRIGATION_UPGRADE_TIER_2);
                entries.add(ModItems.RANGE_UPGRADE_TIER_3);
                entries.add(ModItems.IRRIGATION_UPGRADE_TIER_3);
            })
            .build();

    @Override
    public void onInitialize() {
        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "sequential_hopper"), SEQUENTIAL_HOPPER);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "sequential_hopper"), new BlockItem(SEQUENTIAL_HOPPER, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(MOD_ID, "planter"), PLANTER);
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "planter"), new BlockItem(PLANTER, new Item.Settings()));
        PLANTER_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(MOD_ID, "planter_entity"), BlockEntityType.Builder.create(PlanterBlockEntity::new, PLANTER).build(null));
        com.nexusreds.addons.item.ModItems.registerModItems();

        PLANTER_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(MOD_ID, "planter"),
            new ScreenHandlerType<>(PlanterScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES)
        );

        SEQUENTIAL_HOPPER_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "sequential_hopper_entity"),
            BlockEntityType.Builder.create(SequentialHopperBlockEntity::new, SEQUENTIAL_HOPPER).build(null)
        );

        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "nexus_wrench"), NEXUS_WRENCH);
        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "main_group"), NEXUSREDS_GROUP);
        System.out.println("Nexus Reds Addons: Carregado com sucesso!");
    }
}