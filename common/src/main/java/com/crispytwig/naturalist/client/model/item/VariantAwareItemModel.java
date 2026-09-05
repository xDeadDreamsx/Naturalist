package com.crispytwig.naturalist.client.model.item;

import com.crispytwig.naturalist.server.item.NaturalistBucketItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBakedItemModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Environment(EnvType.CLIENT)
public final class VariantAwareItemModel extends WrapperBakedItemModel {
    private final NaturalistBucketItem item;
    private final Map<Identifier, ItemModel> variants;

    public VariantAwareItemModel(ItemModel fallback, NaturalistBucketItem item, Map<Identifier, ItemModel> variants) {
        super(fallback);
        this.item = item;
        this.variants = variants;
    }

    @Override
    public void update(ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {
        ItemModel selected = VariantItemModels.resolveModelId(this.item, stack, level)
                .map(this.variants::get).orElse(this.wrapped);
        selected.update(state, stack, resolver, displayContext, level, owner, seed);
    }
}
