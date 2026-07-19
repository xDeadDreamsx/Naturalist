package com.crispytwig.naturalist.server.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
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

    public static boolean isVisiblyMoving(Entity entity) {
        return entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
    }

    public static float defaultVoicePitch(RandomSource random) {
        return (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F;
    }

    public static void freezeInPlace(Mob mob) {
        mob.getNavigation().stop();
        mob.setZza(0.0F);
        mob.setXxa(0.0F);
        mob.setDeltaMovement(0.0D, mob.getDeltaMovement().y, 0.0D);
    }
}
