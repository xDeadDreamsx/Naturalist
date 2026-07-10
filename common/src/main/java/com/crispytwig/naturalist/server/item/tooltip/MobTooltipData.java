package com.crispytwig.naturalist.server.item.tooltip;

import com.crispytwig.naturalist.platform.Services;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.Optional;

public record MobTooltipData(ListTag mobs) implements TooltipComponent {
    private static Boolean spawnLoaded;

    public static boolean shouldDisplay() {
        if (spawnLoaded == null) {
            spawnLoaded = Services.PLATFORM.isModLoaded("spawn");
        }
        return spawnLoaded;
    }

    public static Optional<TooltipComponent> forSingleMob(EntityType<?> type, ItemStack stack) {
        if (!shouldDisplay()) {
            return Optional.empty();
        }
        CompoundTag tag = stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY).copyTag();
        tag.merge(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag());
        tag.putString("id", EntityType.getKey(type).toString());
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        if (customName != null) {
            tag.putString("CustomName", customName.getString());
        }
        ListTag mobs = new ListTag();
        mobs.add(tag);
        return Optional.of(new MobTooltipData(mobs));
    }
}
