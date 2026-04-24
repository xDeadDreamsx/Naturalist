package com.starfish_studios.naturalist.server.entity.base;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public abstract class NaturalistAnimal extends Animal {

    protected NaturalistAnimal(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }
}
