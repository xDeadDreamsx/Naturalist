package com.crispytwig.naturalist.server.entity.base;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public interface VariantBucketable extends Bucketable, VariantAnimal {
    @Override
    default void saveToBucketTag(ItemStack stack) {
        Bucketable.saveDefaultDataToBucketTag((Mob & Bucketable) this, stack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> tag.putInt(VARIANT_TAG, this.getVariant()));
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        custom.putInt(VARIANT_TAG, this.getVariant());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }

    @Override
    default void loadFromBucketTag(CompoundTag tag) {
        Bucketable.loadDefaultDataFromBucketTag((Mob & Bucketable) this, tag);
        if (tag.contains(VARIANT_TAG)) {
            this.setVariant(tag.getInt(VARIANT_TAG));
        }
    }
}
