package com.nexusreds.addons;

import com.nexusreds.addons.block.ModBlocks;
import com.nexusreds.addons.block.entity.CopperPipeBlockEntity;
import com.nexusreds.addons.block.entity.PlanterBlockEntity;
import com.nexusreds.addons.block.entity.SequentialHopperBlockEntity;
import com.nexusreds.addons.item.ModItems;
import com.nexusreds.addons.screen.PlanterScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.entity.BlockEntityType;
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

    // O Wrench continua aqui por enquanto (ou pode ir pro ModItems depois!)
    public static final Item NEXUS_WRENCH = new com.nexusreds.addons.item.NexusWrenchItem(new Item.Settings().maxCount(1));

    public static BlockEntityType<SequentialHopperBlockEntity> SEQUENTIAL_HOPPER_ENTITY;
    public static BlockEntityType<PlanterBlockEntity> PLANTER_ENTITY;
    public static ScreenHandlerType<PlanterScreenHandler> PLANTER_SCREEN_HANDLER;
    public static BlockEntityType<CopperPipeBlockEntity> COPPER_PIPE_ENTITY;

    public static final ItemGroup NEXUSREDS_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.SEQUENTIAL_HOPPER))
            .displayName(Text.translatable("itemGroup.nexusreds.main_group"))
            .entries((context, entries) -> {
                entries.add(NEXUS_WRENCH);
                entries.add(ModBlocks.SEQUENTIAL_HOPPER);
                entries.add(ModBlocks.PLANTER);
                //entries.add(ModBlocks.COPPER_PIPE_REDS);
                entries.add(ModItems.UPGRADE_BLANK);
                entries.add(ModItems.RANGE_UPGRADE);
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
        // 1. Registra todos os Blocos e Itens centralizados
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();

        // 2. Registra as Entidades dos Blocos (Apontando para a nova classe ModBlocks)
        PLANTER_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE, 
            Identifier.of(MOD_ID, "planter_entity"), 
            BlockEntityType.Builder.create(PlanterBlockEntity::new, ModBlocks.PLANTER).build(null)
        );

        COPPER_PIPE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "copper_pipe_entity"),
            BlockEntityType.Builder.create(CopperPipeBlockEntity::new, ModBlocks.COPPER_PIPE_REDS).build(null)
        );

        SEQUENTIAL_HOPPER_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Identifier.of(MOD_ID, "sequential_hopper_entity"),
            BlockEntityType.Builder.create(SequentialHopperBlockEntity::new, ModBlocks.SEQUENTIAL_HOPPER).build(null)
        );

        // 3. Registra as Telas (GUI)
        PLANTER_SCREEN_HANDLER = Registry.register(
            Registries.SCREEN_HANDLER,
            Identifier.of(MOD_ID, "planter"),
            new ScreenHandlerType<>(PlanterScreenHandler::new, net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES)
        );

        // 4. Registra a Aba Criativa e o Wrench
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "nexus_wrench"), NEXUS_WRENCH);
        Registry.register(Registries.ITEM_GROUP, Identifier.of(MOD_ID, "main_group"), NEXUSREDS_GROUP);
        
        System.out.println("Nexus Reds Addons: Carregado com sucesso!");
    }
}