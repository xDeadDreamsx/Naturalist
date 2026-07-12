package com.crispytwig.naturalist.server.entity.util;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;

public class SmoothSpeedAnimationController<T extends GeoAnimatable> extends AnimationController<T> {
    private double accumulatedTick;
    private double lastSeekTime = Double.NaN;

    public SmoothSpeedAnimationController(T animatable, String name, int transitionTickTime, AnimationStateHandler<T> animationHandler) {
        super(animatable, name, transitionTickTime, animationHandler);
    }

    @Override
    protected double adjustTick(double tick) {
        if (!this.shouldResetTick) {
            if (Double.isNaN(this.lastSeekTime)) {
                this.lastSeekTime = tick;
            }
            this.accumulatedTick += this.animationSpeedModifier.apply(this.animatable) * Math.max(tick - this.lastSeekTime, 0);
            this.lastSeekTime = tick;
            return this.accumulatedTick;
        }
        if (getAnimationState() != State.STOPPED) {
            this.tickOffset = tick;
        }
        this.shouldResetTick = false;
        this.accumulatedTick = 0;
        this.lastSeekTime = tick;
        return 0;
    }
}
