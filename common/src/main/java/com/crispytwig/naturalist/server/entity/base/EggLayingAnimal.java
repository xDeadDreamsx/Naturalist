package com.crispytwig.naturalist.server.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface EggLayingAnimal {
    boolean hasEgg();
    void setHasEgg(boolean hasEgg);
    boolean isLayingEgg();
    void setLayingEgg(boolean isLayingEgg);
    int getLayEggCounter();
    void setLayEggCounter(int layEggCounter);
    Block getEggBlock();
    TagKey<Block> getEggLayableBlockTag();

    default void onEggLaid(BlockPos pos) {
    }
}
