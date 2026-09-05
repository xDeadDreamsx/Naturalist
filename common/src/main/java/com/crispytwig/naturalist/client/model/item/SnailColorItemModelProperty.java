package com.crispytwig.naturalist.client.model.item;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jspecify.annotations.Nullable;

public final class SnailColorItemModelProperty implements RangeSelectItemModelProperty {
    public static final SnailColorItemModelProperty INSTANCE = new SnailColorItemModelProperty();
    public static final MapCodec<SnailColorItemModelProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

    private SnailColorItemModelProperty() {
    }

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0.0F;
        }
        CompoundTag tag = customData.getUnsafe();
        return tag.getIntOr("Color", 0) / 15.0F;
    }

    @Override
    public MapCodec<SnailColorItemModelProperty> type() {
        return MAP_CODEC;
    }
}
