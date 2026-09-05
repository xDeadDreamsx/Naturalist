package com.crispytwig.naturalist.client.model.item;

import com.crispytwig.naturalist.server.item.KnapsackItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class KnapsackFilledItemModelProperty implements ConditionalItemModelProperty {
    public static final KnapsackFilledItemModelProperty INSTANCE = new KnapsackFilledItemModelProperty();
    public static final MapCodec<KnapsackFilledItemModelProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

    private KnapsackFilledItemModelProperty() {
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed,
                       ItemDisplayContext displayContext) {
        return KnapsackItem.isFilled(stack);
    }

    @Override
    public MapCodec<KnapsackFilledItemModelProperty> type() {
        return MAP_CODEC;
    }
}
