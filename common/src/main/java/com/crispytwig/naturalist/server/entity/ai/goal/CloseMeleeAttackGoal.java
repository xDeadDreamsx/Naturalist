package com.crispytwig.naturalist.server.entity.ai.goal;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.jetbrains.annotations.NotNull;
import net.minecraft.server.level.ServerLevel;

public class CloseMeleeAttackGoal extends MeleeAttackGoal {
    public CloseMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
    }

    @Override
    protected void checkAndPerformAttack(@NotNull LivingEntity target) {
        if (this.mob.distanceToSqr(target) <= Mth.square(this.mob.getBbWidth() * 1.2f) && this.isTimeToAttack()) {
            this.resetAttackCooldown();
            this.mob.swing(InteractionHand.MAIN_HAND);
            if (this.mob.level() instanceof ServerLevel serverLevel) { this.mob.doHurtTarget(serverLevel, target); }
        }
    }
}
