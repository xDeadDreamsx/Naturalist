package com.crispytwig.naturalist.client.model.item;

import com.crispytwig.naturalist.registry.NaturalistMobVariants;
import com.crispytwig.naturalist.server.entity.variant.MobVariantUtil;
import com.crispytwig.naturalist.server.item.NaturalistBucketItem;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedExtraModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Restores the 1.21.1 datapack-driven inventory models, including resource-pack additions. */
@Environment(EnvType.CLIENT)
public final class VariantItemModels {
    public static final String MODEL_FOLDER = "item/variant";

    private VariantItemModels() {
    }

    public static void register() {
        Map<Identifier, NaturalistBucketItem> items = new HashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof NaturalistBucketItem bucketItem) {
                items.put(BuiltInRegistries.ITEM.getKey(item), bucketItem);
            }
        }

        PreparableModelLoadingPlugin.register(
                (sharedState, executor) -> CompletableFuture.supplyAsync(
                        () -> scanExtraModels(sharedState.resourceManager()), executor),
                (extraModels, context) -> {
                    // Item-model bake events run after dependency discovery. Register every
                    // variant here so its parents and textures are resolved before baking.
                    context.addModel(ExtraModelKey.<Boolean>create(() -> "naturalist variant items"),
                            new UnbakedExtraModel<Boolean>() {
                                @Override
                                public void resolveDependencies(Resolver resolver) {
                                    extraModels.values().forEach(model -> model.resolveDependencies(resolver));
                                }

                                @Override
                                public Boolean bake(ModelBaker baker) {
                                    return true;
                                }
                            });
                    context.modifyItemModelAfterBake().register((model, bakeContext) -> {
                        NaturalistBucketItem item = items.get(bakeContext.itemId());
                        if (item == null) {
                            return model;
                        }
                        Map<Identifier, ItemModel> baked = new HashMap<>();
                        extraModels.forEach((id, unbaked) -> baked.put(id,
                                unbaked.bake(bakeContext.bakingContext(), bakeContext.transformation())));
                        return new VariantAwareItemModel(model, item, Map.copyOf(baked));
                    });
                });
    }

    private static Map<Identifier, ItemModel.Unbaked> scanExtraModels(ResourceManager resources) {
        Map<Identifier, ItemModel.Unbaked> models = new HashMap<>();
        for (Identifier file : resources.listResources("models/" + MODEL_FOLDER,
                id -> id.getPath().endsWith(".json")).keySet()) {
            Identifier id = file.withPath(path -> path.substring("models/".length(), path.length() - ".json".length()));
            JsonObject definition = new JsonObject();
            definition.addProperty("type", "minecraft:model");
            definition.addProperty("model", id.toString());
            models.put(id, ItemModels.CODEC.parse(JsonOps.INSTANCE, definition).getOrThrow());
        }
        return Map.copyOf(models);
    }

    public static Optional<Identifier> resolveModelId(NaturalistBucketItem item, ItemStack stack,
                                                     @Nullable Level level) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return Optional.empty();
        }
        Optional<Identifier> id = MobVariantUtil.readVariantId(customData.copyTag(), item.getLegacyVariantNames());
        Level lookupLevel = level != null ? level : Minecraft.getInstance().level;
        if (id.isEmpty() || lookupLevel == null) {
            return Optional.empty();
        }
        return NaturalistMobVariants.findRegistry(item.getVariantEntityType())
                .flatMap(registry -> MobVariantUtil.byId(lookupLevel.registryAccess(), registry, id.get()))
                .flatMap(holder -> holder.value().itemModel());
    }
}
