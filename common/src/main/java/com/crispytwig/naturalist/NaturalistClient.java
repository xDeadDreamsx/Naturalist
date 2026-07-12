package com.crispytwig.naturalist;

import com.crispytwig.naturalist.client.gui.screens.ElephantInventoryScreen;
import com.crispytwig.naturalist.client.renderer.*;
import com.crispytwig.naturalist.registry.NaturalistBlockEntities;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistMenus;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.server.item.KnapsackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

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
        r.register(NaturalistEntityTypes.CRAB.get(), CrabRenderer::new);
        r.register(NaturalistEntityTypes.DEER.get(), DeerRenderer::new);
        r.register(NaturalistEntityTypes.BIRD.get(), BirdRenderer::new);
        r.register(NaturalistEntityTypes.CATERPILLAR.get(), CaterpillarRenderer::new);
        r.register(NaturalistEntityTypes.RHINO.get(), RhinoRenderer::new);
        r.register(NaturalistEntityTypes.LION.get(), LionRenderer::new);
        r.register(NaturalistEntityTypes.ELEPHANT.get(), ElephantRenderer::new);
        r.register(NaturalistEntityTypes.MAMMOTH.get(), MammothRenderer::new);
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
        r.register(NaturalistEntityTypes.BLOBFISH.get(), BlobfishRenderer::new);
        r.register(NaturalistEntityTypes.PIRANHA.get(), PiranhaRenderer::new);
        r.register(NaturalistEntityTypes.WHALE.get(), WhaleRenderer::new);
        r.register(NaturalistEntityTypes.ANT.get(), AntRenderer::new);
        r.register(NaturalistEntityTypes.CARRIED_FOOD.get(), CarriedFoodRenderer::new);
        r.register(NaturalistEntityTypes.MOLE.get(), MoleRenderer::new);
        r.register(NaturalistEntityTypes.DIRT_TRAIL.get(), DirtTrailRenderer::new);
        r.register(NaturalistEntityTypes.RAT.get(), RatRenderer::new);
        r.register(NaturalistEntityTypes.BLACK_BEAR.get(), BlackBearRenderer::new);
        r.register(NaturalistEntityTypes.TIGER.get(), TigerRenderer::new);
        r.register(NaturalistEntityTypes.KOMODO_DRAGON.get(), KomodoDragonRenderer::new);
        r.register(NaturalistEntityTypes.OSTRICH.get(), OstrichRenderer::new);
        r.register(NaturalistEntityTypes.DESERT_SCORPION.get(), DesertScorpionRenderer::new);
        r.register(NaturalistEntityTypes.JUNGLE_SCORPION.get(), JungleScorpionRenderer::new);
        r.register(NaturalistEntityTypes.GREAT_WHITE_SHARK.get(), GreatWhiteSharkRenderer::new);
        r.register(NaturalistEntityTypes.TURKEY.get(), TurkeyRenderer::new);
    }

    @FunctionalInterface
    public interface MenuScreenRegistrar {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> factory);
    }

    public static void registerMenuScreens(MenuScreenRegistrar r) {
        r.register(NaturalistMenus.ELEPHANT.get(), ElephantInventoryScreen::new);
    }

    @FunctionalInterface
    public interface BlockEntityRendererRegistrar {
        <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> provider);
    }

    public static void registerBlockEntityRenderers(BlockEntityRendererRegistrar r) {
        r.register(NaturalistBlockEntities.SNAIL_SHELL.get(), SnailShellRenderer::new);
    }

    public static void registerItemProperties() {
        ClampedItemPropertyFunction color = (stack, level, entity, seed) -> {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            return customData != null ? customData.getUnsafe().getInt("Color") / 15.0f : 0.0f;
        };
        ItemProperties.register(NaturalistRegistry.SNAIL.get(), ResourceLocation.withDefaultNamespace("color"), color);
        ItemProperties.register(NaturalistRegistry.SNAIL_SHELL.get(), ResourceLocation.withDefaultNamespace("color"), color);

        ItemProperties.register(NaturalistRegistry.KNAPSACK.get(),
                Naturalist.location("filled"),
                (stack, level, entity, seed) -> KnapsackItem.isFilled(stack) ? 1.0f : 0.0f);
    }
}
