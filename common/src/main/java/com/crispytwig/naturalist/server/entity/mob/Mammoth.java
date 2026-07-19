package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Mammoth extends Elephant {
    //region Data
    public Mammoth(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public ResourceLocation getFallbackVariantTexture() {
        return Naturalist.location("textures/entity/mammoth/mammoth.png");
    }
    //endregion

    //region Spawning
    public static boolean checkMammothSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos.below());
        return (state.is(BlockTags.DIRT) || state.is(BlockTags.SNOW) || state.is(BlockTags.ICE)) && isBrightEnoughToSpawn(level, pos);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        Mammoth baby = NaturalistEntityTypes.MAMMOTH.get().create(serverLevel);
        if (baby != null) {
            baby.setVariantString(this.getOffspringVariantId(ageableMob, this.random));
        }
        return baby;
    }
    //endregion

    //region Behavior
    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? NaturalistSoundEvents.MAMMOTH_AMBIENT_BABY.get() : NaturalistSoundEvents.MAMMOTH_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return this.isBaby() ? NaturalistSoundEvents.MAMMOTH_HURT_BABY.get() : NaturalistSoundEvents.MAMMOTH_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? NaturalistSoundEvents.MAMMOTH_DEATH_BABY.get() : NaturalistSoundEvents.MAMMOTH_DEATH.get();
    }
    //endregion
}
