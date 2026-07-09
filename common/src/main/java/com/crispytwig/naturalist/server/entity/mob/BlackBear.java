package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlackBear extends Bear {
    //region Behavior
    public BlackBear(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/black_bear/black_bear.png");
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSleeping() ? NaturalistSoundEvents.BLACK_BEAR_SLEEP.get() : NaturalistSoundEvents.BLACK_BEAR_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.BLACK_BEAR_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.BLACK_BEAR_DEATH.get();
    }

    @Override
    protected SoundEvent getEatSound() {
        return NaturalistSoundEvents.BLACK_BEAR_EAT.get();
    }

    @Override
    protected SoundEvent getSniffSound() {
        return NaturalistSoundEvents.BLACK_BEAR_SNIFF.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAttackSound() {
        return NaturalistSoundEvents.BLACK_BEAR_ATTACK.get();
    }

    @Override
    protected void playEatSound() {
    }
    //endregion
}
