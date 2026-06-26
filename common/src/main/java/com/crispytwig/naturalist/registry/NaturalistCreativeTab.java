package com.crispytwig.naturalist.registry;

import com.crispytwig.naturalist.Naturalist;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import com.crispytwig.naturalist.platform.registry.DeferredHolder;
import com.crispytwig.naturalist.platform.registry.DeferredRegister;

public class NaturalistCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Naturalist.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEM_GROUP = CREATIVE_MODE_TABS.register("item_group",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(NaturalistRegistry.CAPTURE_NET.get()::getDefaultInstance)
                    .title(Component.translatable("itemGroup.naturalist.tab"))
                    .displayItems((params, output) -> NaturalistRegistry.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build()
    );
}
