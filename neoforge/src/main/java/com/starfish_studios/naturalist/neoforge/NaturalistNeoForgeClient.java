package com.starfish_studios.naturalist.neoforge;

import com.starfish_studios.naturalist.NaturalistClient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class NaturalistNeoForgeClient {
    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(NaturalistNeoForgeClient::registerRenderers);
        modEventBus.addListener(NaturalistNeoForgeClient::clientSetup);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        NaturalistClient.registerRenderers(event::registerEntityRenderer);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(NaturalistClient::registerItemProperties);
    }
}
