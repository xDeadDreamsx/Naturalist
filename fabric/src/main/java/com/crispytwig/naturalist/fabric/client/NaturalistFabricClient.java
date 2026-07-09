package com.crispytwig.naturalist.fabric.client;

import com.crispytwig.naturalist.NaturalistClient;
import com.crispytwig.naturalist.client.model.item.VariantAwareItemModel;
import com.crispytwig.naturalist.client.model.item.VariantItemModels;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.server.item.NaturalistBucketItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricBakedModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NaturalistFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NaturalistClient.registerRenderers(EntityRendererRegistry::register);

        NaturalistClient.registerItemProperties();
        NaturalistClient.registerMenuScreens(MenuScreens::register);

        registerVariantItemModels();

        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.ORANGE_STARFISH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.PURPLE_STARFISH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.BLUE_STARFISH.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(NaturalistRegistry.RED_STARFISH.get(), RenderType.cutout());
    }

    private static void registerVariantItemModels() {
        Map<ResourceLocation, NaturalistBucketItem> variantItems = VariantItemModels.collectVariantItems();
        PreparableModelLoadingPlugin.register(
                (resourceManager, executor) -> CompletableFuture.supplyAsync(() -> VariantItemModels.scanExtraModels(resourceManager), executor),
                (extraModels, context) -> {
                    context.addModels(extraModels);
                    context.modifyModelAfterBake().register((model, ctx) -> {
                        ModelResourceLocation topLevelId = ctx.topLevelId();
                        if (model != null && topLevelId != null && "inventory".equals(topLevelId.getVariant())) {
                            NaturalistBucketItem item = variantItems.get(topLevelId.id());
                            if (item != null) {
                                return new VariantAwareItemModel(model, item,
                                        id -> ((FabricBakedModelManager) Minecraft.getInstance().getModelManager()).getModel(id));
                            }
                        }
                        return model;
                    });
                });
    }
}
