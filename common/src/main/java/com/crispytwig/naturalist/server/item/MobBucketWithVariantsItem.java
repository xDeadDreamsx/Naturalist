package com.crispytwig.naturalist.server.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MobBucketWithVariantsItem extends MobBucketItem {
    private final String tooltipPrefix;
    private final String[] variantNames;

    public MobBucketWithVariantsItem(Supplier<? extends EntityType<?>> entitySupplier, Supplier<? extends Fluid> fluidSupplier, Supplier<? extends SoundEvent> soundSupplier, @NotNull Properties properties, String tooltipPrefix, String[] variantNames) {
        super(entitySupplier.get(), fluidSupplier.get(), soundSupplier.get(), properties);
        this.tooltipPrefix = tooltipPrefix;
        this.variantNames = variantNames;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("Variant")) {
            int variant = Math.floorMod(tag.getInt("Variant"), this.variantNames.length);
            tooltip.add(Component.translatable(this.tooltipPrefix + this.variantNames[variant]).withStyle(ChatFormatting.GRAY));
        }
    }
}
