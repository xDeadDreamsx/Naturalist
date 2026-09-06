package com.crispytwig.naturalist.server.entity.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.AnimationState;

/**
 * Credit to <a href="https://github.com/platypushasnohat/Unusual-Prehistory-2/blob/main/src/main/java/com/barl_inc/unusual_prehistory/entity/utils/SmoothAnimationState.java"> UP2's SmoothAnimationState</a>
 */
public class SmoothAnimationState extends AnimationState {
    public static final float ACTIVE_THRESHOLD = 0.05F;
    private static final float STOP_THRESHOLD = 0.001F;
    private static final float DEFAULT_LERP_SPEED = 0.5F;
    private static final float POSE_LERP_SPEED = 0.25F;
    private static final float INSTANT_LERP_SPEED = 1.0F;

    private final float lerpSpeed;
    private float factorOld;
    private float factor;

    /*
     * Minecraft 1.21.1's AnimationState accumulated animation time incrementally through
     * updateTime(ageInTicks, speed). Minecraft 26.2 replaced that API with getTimeInMillis(),
     * which reports the total time since the state started. Multiplying that total by a speed
     * that changes every frame (as Naturalist's movement animations do) causes increasingly
     * large phase jumps. Keep the old accumulated clock here so variable movement speeds are
     * integrated exactly like they were before the render-state migration.
     */
    private long lastAnimationTime;
    private long accumulatedAnimationTime;

    public SmoothAnimationState() {
        this(DEFAULT_LERP_SPEED);
    }

    public SmoothAnimationState(float lerpSpeed) {
        this.lerpSpeed = lerpSpeed;
    }

    public static SmoothAnimationState pose() {
        return new SmoothAnimationState(POSE_LERP_SPEED);
    }

    public static SmoothAnimationState instant() {
        return new SmoothAnimationState(INSTANT_LERP_SPEED);
    }

    @Override
    public void start(int tickCount) {
        super.start(tickCount);
        this.lastAnimationTime = 0L;
        this.accumulatedAnimationTime = 0L;
    }

    @Override
    public void startIfStopped(int tickCount) {
        if (!this.isStarted()) {
            this.start(tickCount);
        }
    }

    @Override
    public void animateWhen(boolean condition, int tickCount) {
        this.factorOld = this.factor;
        this.factor = Mth.clamp(this.factor + ((condition ? 1.0F : 0.0F) - this.factor) * this.lerpSpeed, 0.0F, 1.0F);
        if (condition) {
            this.startIfStopped(tickCount);
        } else if (this.factor < STOP_THRESHOLD) {
            this.stop();
        }
    }

    /**
     * 1.21.1-compatible variable-speed animation clock.
     *
     * <p>Using a delta rather than {@code getTimeInMillis(ageInTicks) * speed} is important:
     * movement animation speed is recalculated every frame, so the latter re-scales the entire
     * history every time the entity accelerates or decelerates.</p>
     */
    public void updateTime(float ageInTicks, float speed) {
        if (!this.isStarted()) {
            return;
        }
        long currentAnimationTime = this.getTimeInMillis(ageInTicks);
        long elapsed = Math.max(0L, currentAnimationTime - this.lastAnimationTime);
        this.accumulatedAnimationTime += (long) (elapsed * speed);
        this.lastAnimationTime = currentAnimationTime;
    }

    public long getAccumulatedTime() {
        return this.accumulatedAnimationTime;
    }

    public float factor(float partialTick) {
        return Mth.lerp(partialTick, this.factorOld, this.factor);
    }

}
