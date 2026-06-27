package com.crispytwig.naturalist.server.entity.ai.goal;

import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;

public class PetFollowOwnerGoal extends FollowOwnerGoal {
    private final FollowingPet pet;

    public PetFollowOwnerGoal(TamableAnimal mob, double speedModifier, float startDistance, float stopDistance) {
        super(mob, speedModifier, startDistance, stopDistance);
        this.pet = (FollowingPet) mob;
    }

    @Override
    public boolean canUse() {
        return this.pet.isFollowingOwner() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.pet.isFollowingOwner() && super.canContinueToUse();
    }
}
