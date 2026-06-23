package com.nexusreds.addons;

import com.nexusreds.addons.block.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class NexusRedsClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Vincula o nosso funil à camada CUTOUT, permitindo pixels transparentes nas texturas
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.SEQUENTIAL_HOPPER, RenderLayer.getCutout());
        
        // Vincula o novo Cano de Cobre para que a sua malha 3D e transparências funcionem perfeitamente
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.COPPER_PIPE_REDS, RenderLayer.getCutout());

        net.minecraft.client.gui.screen.ingame.HandledScreens.register(
            NexusRedsMod.PLANTER_SCREEN_HANDLER,
            com.nexusreds.addons.client.screen.PlanterScreen::new
        );
    }
}