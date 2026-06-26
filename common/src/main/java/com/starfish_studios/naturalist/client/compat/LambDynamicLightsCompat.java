package com.starfish_studios.naturalist.client.compat;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.NonNull;

public class LambDynamicLightsCompat implements DynamicLightsInitializer {
    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager().onRegisterEvent()
                .register(
                        ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "firefly"),
                        registerContext -> registerContext.register(
                                NaturalistEntityTypes.FIREFLY.get(),
                                FireflyLuminance.INSTANCE
                        )
                );
    }

    @Override
    public void onInitializeDynamicLights(@NonNull ItemLightSourceManager itemLightSourceManager) {}
}
