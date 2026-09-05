package com.crispytwig.naturalist.client.model.item;

import com.crispytwig.naturalist.Naturalist;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;

public final class NaturalistItemModelProperties {
    private NaturalistItemModelProperties() {
    }

    public static void register() {
        RangeSelectItemModelProperties.ID_MAPPER.put(
                Naturalist.location("snail_color"), SnailColorItemModelProperty.MAP_CODEC);
        ConditionalItemModelProperties.ID_MAPPER.put(
                Naturalist.location("knapsack_filled"), KnapsackFilledItemModelProperty.MAP_CODEC);
    }
}
