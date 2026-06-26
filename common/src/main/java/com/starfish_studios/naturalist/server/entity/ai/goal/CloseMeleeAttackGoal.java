package com.starfish_studios.naturalist.server.entity.ai.goal;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.jetbrains.annotations.NotNull;

public class CloseMeleeAttackGoal extends MeleeAttackGoal {
    public CloseMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target) {
        double reach = Mth.square(this.mob.getBbWidth() * 1.2f);
        if (this.mob.distanceToSqr(target) <= reach && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            this.mob.doHurtTarget(target);
        }
    }
}
