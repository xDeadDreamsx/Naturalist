package com.starfish_studios.naturalist.registry;

import com.starfish_studios.naturalist.Naturalist;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NaturalistCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Naturalist.MOD_ID);

    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEM_GROUP = CREATIVE_MODE_TABS.register("item_group",
            () -> CreativeModeTab.builder()
                    .icon(NaturalistRegistry.CAPTURE_NET.get()::getDefaultInstance)
                    .title(Component.translatable("itemGroup.naturalist.tab"))
                    .displayItems((params, output) -> NaturalistRegistry.ITEMS.getEntries().forEach(entry -> output.accept(entry.get())))
                    .build()
    );
}
