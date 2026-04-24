package com.starfish_studios.naturalist.server.item;

import net.minecraft.sounds.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.material.*;

import java.util.function.*;

public class CaughtMobWithVariantsItem extends CaughtMobItem {

    @SuppressWarnings("unused")
    public CaughtMobWithVariantsItem(Supplier<? extends EntityType<?>> entitySupplier, Supplier<? extends Fluid> fluidSupplier, Supplier<? extends SoundEvent> soundSupplier, int variantCount, Properties properties) {
        super(entitySupplier, fluidSupplier, soundSupplier, properties);
    }

}
