package com.crispytwig.naturalist;

import com.crispytwig.naturalist.client.gui.screens.ElephantInventoryScreen;
import com.crispytwig.naturalist.client.renderer.*;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistMenus;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.server.item.KnapsackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.component.CustomData;

@Environment(EnvType.CLIENT)
public final class NaturalistClient {
    private NaturalistClient() {
    }

    @FunctionalInterface
    public interface RendererRegistrar {
        <T extends Entity> void register(EntityType<? extends T> type, EntityRendererProvider<T> provider);
    }

    public static void registerRenderers(RendererRegistrar r) {
        r.register(NaturalistEntityTypes.SNAIL.get(), SnailRenderer::new);
        r.register(NaturalistEntityTypes.BEAR.get(), BearRenderer::new);
        r.register(NaturalistEntityTypes.BUTTERFLY.get(), ButterflyRenderer::new);
        r.register(NaturalistEntityTypes.FIREFLY.get(), FireflyRenderer::new);
        r.register(NaturalistEntityTypes.SNAKE.get(), SnakeRenderer::new);
        r.register(NaturalistEntityTypes.CORAL_SNAKE.get(), SnakeRenderer::new);
        r.register(NaturalistEntityTypes.RATTLESNAKE.get(), SnakeRenderer::new);
        r.register(NaturalistEntityTypes.CRAB.get(), CrabRenderer::new);
        r.register(NaturalistEntityTypes.DEER.get(), DeerRenderer::new);
        r.register(NaturalistEntityTypes.BLUEJAY.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.CARDINAL.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.CANARY.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.ROBIN.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.FINCH.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.SPARROW.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.CATERPILLAR.get(), CaterpillarRenderer::new);
        r.register(NaturalistEntityTypes.RHINO.get(), RhinoRenderer::new);
        r.register(NaturalistEntityTypes.LION.get(), LionRenderer::new);
        r.register(NaturalistEntityTypes.ELEPHANT.get(), ElephantRenderer::new);
        r.register(NaturalistEntityTypes.ZEBRA.get(), ZebraRenderer::new);
        r.register(NaturalistEntityTypes.GIRAFFE.get(), GiraffeRenderer::new);
        r.register(NaturalistEntityTypes.HIPPO.get(), HippoRenderer::new);
        r.register(NaturalistEntityTypes.VULTURE.get(), VultureRenderer::new);
        r.register(NaturalistEntityTypes.BOAR.get(), BoarRenderer::new);
        r.register(NaturalistEntityTypes.DRAGONFLY.get(), DragonflyRenderer::new);
        r.register(NaturalistEntityTypes.CATFISH.get(), CatfishRenderer::new);
        r.register(NaturalistEntityTypes.ALLIGATOR.get(), AlligatorRenderer::new);
        r.register(NaturalistEntityTypes.BASS.get(), BassRenderer::new);
        r.register(NaturalistEntityTypes.LIZARD.get(), LizardRenderer::new);
        r.register(NaturalistEntityTypes.LIZARD_TAIL.get(), LizardTailRenderer::new);
        r.register(NaturalistEntityTypes.TORTOISE.get(), TortoiseRenderer::new);
        r.register(NaturalistEntityTypes.DUCK.get(), DuckRenderer::new);
        r.register(NaturalistEntityTypes.DUCK_EGG.get(), ThrownItemRenderer::new);
        r.register(NaturalistEntityTypes.STARFISH.get(), StarfishRenderer::new);
        r.register(NaturalistEntityTypes.CLAM.get(), ClamRenderer::new);
        r.register(NaturalistEntityTypes.GIANT_ISOPOD.get(), GiantIsopodRenderer::new);
        r.register(NaturalistEntityTypes.JELLYFISH.get(), JellyfishRenderer::new);
        r.register(NaturalistEntityTypes.ANGLERFISH.get(), AnglerfishRenderer::new);
        r.register(NaturalistEntityTypes.RAY.get(), RayRenderer::new);
    }

    @FunctionalInterface
    public interface MenuScreenRegistrar {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> factory);
    }

    public static void registerMenuScreens(MenuScreenRegistrar r) {
        r.register(NaturalistMenus.ELEPHANT.get(), ElephantInventoryScreen::new);
    }

    public static void registerItemProperties() {
        ItemProperties.register(NaturalistRegistry.BUTTERFLY.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant") / 7.0f;
                    }
                    return 0.0f;
                });

        ItemProperties.register(NaturalistRegistry.CRAB.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant") / 5.0f;
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

        ItemProperties.register(NaturalistRegistry.STARFISH_BUCKET.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant") / 3.0f;
                    }
                    return 0.0f;
                });

        ItemProperties.register(NaturalistRegistry.GIANT_ISOPOD_BUCKET.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant");
                    }
                    return 0.0f;
                });

        ItemProperties.register(NaturalistRegistry.JELLYFISH_BUCKET.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant") / 4.0f;
                    }
                    return 0.0f;
                });

        ItemProperties.register(NaturalistRegistry.ANGLERFISH_BUCKET.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant");
                    }
                    return 0.0f;
                });

        ItemProperties.register(NaturalistRegistry.RAY_BUCKET.get(),
                ResourceLocation.withDefaultNamespace("variant"),
                (stack, level, entity, seed) -> {
                    CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
                    if (customData != null) {
                        return customData.copyTag().getInt("Variant");
                    }
                    return 0.0f;
                });

        ItemProperties.register(NaturalistRegistry.KNAPSACK.get(),
                ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "filled"),
                (stack, level, entity, seed) -> KnapsackItem.isFilled(stack) ? 1.0f : 0.0f);
    }
}
