package com.crispytwig.naturalist.server.entity.base;

import net.minecraft.nbt.CompoundTag;

public interface HuntingAnimal {
    int HUNTING_COOLDOWN_TICKS = 2400;
    String HUNTING_COOLDOWN_TAG = "HuntingCooldown";

    int getHuntingCooldown();

    void setHuntingCooldown(int ticks);

    default boolean hasHuntingCooldown() {
        return this.getHuntingCooldown() <= 0;
    }

    default void startHuntingCooldown() {
        this.setHuntingCooldown(HUNTING_COOLDOWN_TICKS);
    }

    default void tickHuntingCooldown() {
        int cooldown = this.getHuntingCooldown();
        if (cooldown > 0) {
            this.setHuntingCooldown(cooldown - 1);
        }
    }

    default void addHuntingCooldownSaveData(CompoundTag compound) {
        compound.putInt(HUNTING_COOLDOWN_TAG, this.getHuntingCooldown());
    }

    default void readHuntingCooldownSaveData(CompoundTag compound) {
        this.setHuntingCooldown(compound.getInt(HUNTING_COOLDOWN_TAG));
    }
}
