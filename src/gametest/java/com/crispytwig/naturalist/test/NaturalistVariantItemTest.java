package com.crispytwig.naturalist.test;

import com.crispytwig.naturalist.client.model.item.VariantAwareItemModel;
import com.crispytwig.naturalist.registry.NaturalistMobVariants;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.item.NaturalistBucketItem;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Checks the actual rendered item sprite, including legacy integer variant data. */
@SuppressWarnings("UnstableApiUsage")
final class NaturalistVariantItemTest {
    private NaturalistVariantItemTest() {
    }

    static void verify(ClientGameTestContext context) {
        context.runOnClient(client -> {
            int checked = 0;
            int legacyChecked = 0;
            ItemModelResolver resolver = new ItemModelResolver(client.getModelManager());
            for (Item item : BuiltInRegistries.ITEM) {
                if (!(item instanceof NaturalistBucketItem bucket)) continue;
                var registryKey = NaturalistMobVariants.findRegistry(bucket.getVariantEntityType());
                if (registryKey.isEmpty()) continue;
                var registry = client.level.registryAccess().lookup(registryKey.get());
                if (registry.isEmpty()) continue;
                if (!(client.getModelManager().getItemModel(BuiltInRegistries.ITEM.getKey(item))
                        instanceof VariantAwareItemModel)) {
                    throw new AssertionError("Variant item model wrapper missing: " + item);
                }
                ItemStack stack = new ItemStack(item);
                for (var holder : registry.get().listElements().toList()) {
                    var model = holder.value().itemModel();
                    if (model.isEmpty()) continue;
                    Identifier variantId = holder.unwrapKey().orElseThrow().identifier();
                    Identifier modelId = model.get();
                    Identifier modelFile = modelId.withPath(path -> "models/" + path + ".json");
                    Identifier expectedSprite;
                    try (var reader = client.getResourceManager().getResource(modelFile).orElseThrow().openAsReader()) {
                        expectedSprite = Identifier.parse(JsonParser.parseReader(reader).getAsJsonObject()
                                .getAsJsonObject("textures").get("layer0").getAsString());
                    } catch (Exception e) {
                        throw new AssertionError("Cannot read variant model " + modelFile, e);
                    }

                    CompoundTag tag = new CompoundTag();
                    tag.putString(DataDrivenVariantAnimal.VARIANT_TAG, variantId.toString());
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    verifySprite(client, resolver, stack, expectedSprite, variantId.toString());
                    checked++;

                    String[] legacy = bucket.getLegacyVariantNames();
                    if (legacy != null) {
                        for (int i = 0; i < legacy.length; i++) {
                            if (variantId.equals(Identifier.fromNamespaceAndPath("naturalist", legacy[i]))) {
                                tag.putInt(DataDrivenVariantAnimal.VARIANT_TAG, i);
                                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                                verifySprite(client, resolver, stack, expectedSprite, "legacy " + i);
                                legacyChecked++;
                            }
                        }
                    }
                }
            }
            if (checked != 26 || legacyChecked == 0) {
                throw new AssertionError("Incomplete variant coverage: " + checked + " models, " + legacyChecked + " legacy values");
            }
            System.out.println("NATURALIST_VARIANT_ITEMS: " + checked + " rendered variant sprites, "
                    + legacyChecked + " legacy variants");
        });
    }

    private static void verifySprite(Minecraft client, ItemModelResolver resolver, ItemStack stack,
                                     Identifier expected, String variant) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver.updateForTopItem(state, stack, ItemDisplayContext.GUI, client.level, client.player, 0);
        if (state.isEmpty()) {
            throw new AssertionError("Empty rendered model for " + stack + " / " + variant);
        }
        Identifier actual = state.pickParticleMaterial(RandomSource.create(0L)).sprite().contents().name();
        if (!actual.equals(expected)) {
            throw new AssertionError(stack + " / " + variant + ": expected " + expected + ", rendered " + actual);
        }
    }
}
