package com.crispytwig.naturalist.fabric.client;

import com.crispytwig.naturalist.NaturalistClient;
import com.crispytwig.naturalist.NaturalistClientConfig;
import com.crispytwig.naturalist.fabric.config.FabricNaturalistClientConfig;
import com.crispytwig.naturalist.client.particle.CaptureNetSwingParticle;
import com.crispytwig.naturalist.registry.NaturalistParticleTypes;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ChunkSectionLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.EntityRenderers;


public class NaturalistFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FabricNaturalistClientConfig.load();
        NaturalistClientConfig.setGlowGoopTooltip(FabricNaturalistClientConfig::isGlowGoopTooltipEnabled);

        NaturalistClient.registerLayerDefinitions((location, definition) ->
                ModelLayerRegistry.registerModelLayer(location, definition::get));
        NaturalistClient.registerRenderers(EntityRenderers::register);

        NaturalistClient.registerItemProperties();
        NaturalistClient.registerMenuScreens(MenuScreens::register);


        ParticleProviderRegistry.getInstance().register(NaturalistParticleTypes.CAPTURE_NET_SWING.get(), CaptureNetSwingParticle.Provider::new);

        ChunkSectionLayerMap.putBlock(NaturalistRegistry.ORANGE_STARFISH.get(), ChunkSectionLayer.CUTOUT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.PURPLE_STARFISH.get(), ChunkSectionLayer.CUTOUT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.BLUE_STARFISH.get(), ChunkSectionLayer.CUTOUT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.RED_STARFISH.get(), ChunkSectionLayer.CUTOUT);

        ChunkSectionLayerMap.putBlock(NaturalistRegistry.SNAIL_EGGS.get(), ChunkSectionLayer.CUTOUT);

        ChunkSectionLayerMap.putBlock(NaturalistRegistry.AZURE_FROGLASS.get(), ChunkSectionLayer.TRANSLUCENT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.VERDANT_FROGLASS.get(), ChunkSectionLayer.TRANSLUCENT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.CRIMSON_FROGLASS.get(), ChunkSectionLayer.TRANSLUCENT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.AZURE_FROGLASS_PANE.get(), ChunkSectionLayer.TRANSLUCENT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.VERDANT_FROGLASS_PANE.get(), ChunkSectionLayer.TRANSLUCENT);
        ChunkSectionLayerMap.putBlock(NaturalistRegistry.CRIMSON_FROGLASS_PANE.get(), ChunkSectionLayer.TRANSLUCENT);
    }

}
