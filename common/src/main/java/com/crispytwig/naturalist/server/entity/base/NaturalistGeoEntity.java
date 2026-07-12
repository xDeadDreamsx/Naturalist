package com.crispytwig.naturalist.server.entity.base;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimationState;

public interface NaturalistGeoEntity extends GeoEntity {
    double SMALL_FISH_LIMB_SWING = 0.25;
    double LARGE_FISH_LIMB_SWING = 0.5;

    default double movementAnimationSpeed(AnimationState<?> state, double tuned) {
        double gaitLimbSwing = Math.min(8.64 * ((LivingEntity) this).getAttributeValue(Attributes.MOVEMENT_SPEED), 1.0);
        return movementAnimationSpeed(state, tuned, gaitLimbSwing * 0.65);
    }

    default double movementAnimationSpeed(AnimationState<?> state, double tuned, double referenceLimbSwing) {
        return tuned * Mth.clamp(state.getLimbSwingAmount() / Math.max(referenceLimbSwing, 0.05), 0.4, 2.0);
    }
}
