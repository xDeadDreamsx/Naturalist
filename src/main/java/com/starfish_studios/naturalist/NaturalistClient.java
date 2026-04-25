package com.starfish_studios.naturalist;

import com.starfish_studios.naturalist.client.renderer.*;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.registry.NaturalistRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class NaturalistClient {

    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NaturalistEntityTypes.SNAIL.get(), SnailRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.BEAR.get(), BearRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.BUTTERFLY.get(), ButterflyRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.FIREFLY.get(), FireflyRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.SNAKE.get(), SnakeRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.CORAL_SNAKE.get(), SnakeRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.RATTLESNAKE.get(), SnakeRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.DEER.get(), DeerRenderer::new);

        event.registerEntityRenderer(NaturalistEntityTypes.BLUEJAY.get(), BirdRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.CARDINAL.get(), BirdRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.CANARY.get(), BirdRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.ROBIN.get(), BirdRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.FINCH.get(), BirdRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.SPARROW.get(), BirdRenderer::new);

        event.registerEntityRenderer(NaturalistEntityTypes.CATERPILLAR.get(), CaterpillarRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.RHINO.get(), RhinoRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.LION.get(), LionRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.ELEPHANT.get(), ElephantRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.ZEBRA.get(), ZebraRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.GIRAFFE.get(), GiraffeRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.HIPPO.get(), HippoRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.VULTURE.get(), VultureRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.BOAR.get(), BoarRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.DRAGONFLY.get(), DragonflyRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.CATFISH.get(), CatfishRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.ALLIGATOR.get(), AlligatorRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.BASS.get(), BassRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.LIZARD.get(), LizardRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.LIZARD_TAIL.get(), LizardTailRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.TORTOISE.get(), TortoiseRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.DUCK.get(), DuckRenderer::new);
        event.registerEntityRenderer(NaturalistEntityTypes.DUCK_EGG.get(), ThrownItemRenderer::new);
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
    }

    public static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(NaturalistRegistry.BUTTERFLY.get(),
                    ResourceLocation.withDefaultNamespace("variant"),
                    (stack, level, entity, seed) -> {
                        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                        if (customData != null) {
                            return customData.copyTag().getInt("Variant") / 7.0f;
                        }
                        return 0.0f;
                    });

            ItemProperties.register(NaturalistRegistry.SNAIL_BUCKET.get(),
                    ResourceLocation.withDefaultNamespace("color"),
                    (stack, level, entity, seed) -> {
                        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                        if (customData != null) {
                            return customData.copyTag().getInt("Color") / 15.0f;
                        }
                        return 0.0f;
                    });
        });
    }
}
