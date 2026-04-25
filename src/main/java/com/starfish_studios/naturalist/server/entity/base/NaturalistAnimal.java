package com.starfish_studios.naturalist.server.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public abstract class NaturalistAnimal extends Animal {

    protected NaturalistAnimal(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean checkNaturalistAnimalSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.DIRT) && isBrightEnoughToSpawn(level, pos);
    }
}
