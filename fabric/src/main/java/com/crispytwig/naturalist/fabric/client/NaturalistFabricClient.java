package com.crispytwig.naturalist.fabric.client;

import com.crispytwig.naturalist.NaturalistClient;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;

public class NaturalistFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NaturalistClient.registerRenderers(EntityRendererRegistry::register);

        NaturalistClient.registerItemProperties();
        NaturalistClient.registerMenuScreens(MenuScreens::register);

        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.ORANGE_STARFISH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.PURPLE_STARFISH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.BLUE_STARFISH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.RED_STARFISH.get(), RenderType.cutout());
    }
}
