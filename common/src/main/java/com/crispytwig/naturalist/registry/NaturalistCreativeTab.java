package com.crispytwig.naturalist.registry;

import com.crispytwig.naturalist.Naturalist;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import com.crispytwig.naturalist.platform.registry.DeferredHolder;
import com.crispytwig.naturalist.platform.registry.DeferredRegister;

public class NaturalistCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Naturalist.MOD_ID);

    private static final int[] STARFISH_TAB_ORDER = {3, 0, 2, 1};
    private static final int[] JELLYFISH_TAB_ORDER = {0, 1, 2, 3, 4};
    private static final int[] ANGLERFISH_TAB_ORDER = {0, 1};
    private static final int[] RAY_TAB_ORDER = {0, 1, 2};

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEM_GROUP = CREATIVE_MODE_TABS.register("item_group",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(NaturalistRegistry.CAPTURE_NET.get()::getDefaultInstance)
                    .title(Component.translatable("itemGroup.naturalist.tab"))
                    .displayItems((params, output) -> NaturalistRegistry.ITEMS.getEntries().forEach(entry -> {
                        Item item = entry.get();
                        if (item == NaturalistRegistry.STARFISH_BUCKET.get()) {
                            acceptVariants(output, item, STARFISH_TAB_ORDER);
                        } else if (item == NaturalistRegistry.JELLYFISH_BUCKET.get()) {
                            acceptVariants(output, item, JELLYFISH_TAB_ORDER);
                        } else if (item == NaturalistRegistry.ANGLERFISH_BUCKET.get()) {
                            acceptVariants(output, item, ANGLERFISH_TAB_ORDER);
                        } else if (item == NaturalistRegistry.RAY_BUCKET.get()) {
                            acceptVariants(output, item, RAY_TAB_ORDER);
                        } else {
                            output.accept(item);
                        }
                    }))
                    .build()
    );

    private static void acceptVariants(CreativeModeTab.Output output, Item item, int[] order) {
        for (int variant : order) {
            ItemStack stack = new ItemStack(item);
            CompoundTag tag = new CompoundTag();
            tag.putInt("Variant", variant);
            CustomData data = CustomData.of(tag);
            stack.set(DataComponents.CUSTOM_DATA, data);
            stack.set(DataComponents.BUCKET_ENTITY_DATA, data);
            output.accept(stack);
        }
    }
}
