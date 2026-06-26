package com.crispytwig.naturalist.fabric.client;

import com.crispytwig.naturalist.NaturalistClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class NaturalistFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NaturalistClient.registerRenderers(EntityRendererRegistry::register);

        NaturalistClient.registerItemProperties();
    }
}
