package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.base.MultipartMob;
import com.crispytwig.naturalist.server.entity.util.SmoothAnimationState;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Shared Naturalist entity model adapter for Minecraft 26.x's render-state based renderer.
 *
 * <p>The entity reference is copied into {@link NaturalistRenderState} by the renderer so the
 * existing Naturalist animation code can be retained while the mod is migrated to Mojang's
 * render-state API.</p>
 */
public abstract class NaturalistEntityModel<E extends Entity> extends EntityModel<NaturalistRenderState<E>> {
    private static final double GAIT_FACTOR = 0.65D;
    private static final double LIMB_SWING_PER_SPEED = 8.64D;
    protected static final float IDLE_FADE_SCALE = 2.5F;

    public static final double SMALL_SWIMMER_LIMB_SWING = 0.25D;
    public static final double LARGE_SWIMMER_LIMB_SWING = 0.5D;

    private final Map<AnimationDefinition, KeyframeAnimation> bakedAnimations = new IdentityHashMap<>();
    private ModelPart animationRoot;

    protected NaturalistEntityModel(ModelPart root) {
        super(root);
    }

    protected NaturalistEntityModel(ModelPart root, Function<Identifier, RenderType> renderType) {
        super(root, renderType);
    }

    /**
     * Name used by legacy Naturalist animations for the model root itself.
     *
     * <p>Before 26.x, Naturalist's HierarchicalModel adapter special-cased this name in
     * getAnyDescendantWithName(). Minecraft 26.x bakes animations directly against a ModelPart
     * tree instead, so we recreate that synthetic parent level for every Naturalist model.</p>
     */
    protected String getRootPartName() {
        return "root";
    }

    private ModelPart animationRoot() {
        if (this.animationRoot == null) {
            this.animationRoot = new ModelPart(List.of(), Map.of(this.getRootPartName(), this.root()));
        }
        return this.animationRoot;
    }

    /**
     * Legacy KeyframeAnimations silently ignored animation channels that did not resolve to a
     * model part. AnimationDefinition#bake in 26.2 throws instead. Several Naturalist animation
     * files contain stale Blockbench bone names (for example Lion tail1/tail3), so retain the
     * old forgiving behaviour by removing only unresolved channels before baking.
     */
    private boolean hasAnimationPart(String name) {
        if (name.equals(this.getRootPartName())) {
            return true;
        }
        return this.root().getAllParts().anyMatch(part -> part.hasChild(name));
    }

    private AnimationDefinition compatibleDefinition(AnimationDefinition definition) {
        var filtered = new LinkedHashMap<>(definition.boneAnimations());
        filtered.entrySet().removeIf(entry -> !this.hasAnimationPart(entry.getKey()));
        if (filtered.size() == definition.boneAnimations().size()) {
            return definition;
        }
        return new AnimationDefinition(definition.lengthInSeconds(), definition.looping(), filtered);
    }

    @Override
    public final void setupAnim(NaturalistRenderState<E> state) {
        super.setupAnim(state);
        E entity = state.entity;
        if (entity == null) {
            return;
        }
        this.setupAnimations(entity, state.walkAnimationPos, state.walkAnimationSpeed, state.ageInTicks,
                state.partialTick, state.yRot, state.xRot);
    }

    protected abstract void setupAnimations(E entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                                            float partialTick, float netHeadYaw, float headPitch);

    protected static void applyHeadLook(ModelPart part, float netHeadYaw, float headPitch) {
        part.xRot += headPitch * Mth.DEG_TO_RAD;
        part.yRot += netHeadYaw * Mth.DEG_TO_RAD;
    }

    protected void rotatePart(ModelPart part, float dx, float dy, float dz) {
        part.xRot += dx;
        part.yRot += dy;
        part.zRot += dz;
    }

    protected void bend(ModelPart part, MultipartMob mob, int segment, float partialTick) {
        this.rotatePart(part,
                mob.getSegmentPitchOffset(segment, partialTick) * Mth.DEG_TO_RAD,
                mob.getSegmentYawOffset(segment, partialTick) * Mth.DEG_TO_RAD,
                0.0F);
    }

    protected static float movementAnimationSpeed(LivingEntity entity, float limbSwingAmount, float baseSpeed) {
        double gaitLimbSwing = Math.min(LIMB_SWING_PER_SPEED * entity.getAttributeValue(Attributes.MOVEMENT_SPEED), 1.0D) * GAIT_FACTOR;
        return movementAnimationSpeed(entity, limbSwingAmount, baseSpeed, gaitLimbSwing);
    }

    protected static float movementAnimationSpeed(LivingEntity entity, float limbSwingAmount, float baseSpeed, double referenceLimbSwing) {
        return movementAnimationSpeed(entity, limbSwingAmount, baseSpeed, referenceLimbSwing, 0.4F);
    }

    protected static float movementAnimationSpeed(LivingEntity entity, float limbSwingAmount, float baseSpeed,
                                                  double referenceLimbSwing, float minSpeed) {
        return baseSpeed * Mth.clamp(limbSwingAmount / (float) Math.max(referenceLimbSwing, 0.05D), minSpeed, 2.0F);
    }

    private KeyframeAnimation animation(AnimationDefinition definition) {
        return this.bakedAnimations.computeIfAbsent(definition,
                d -> this.compatibleDefinition(d).bake(this.animationRoot()));
    }

    protected void animateSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks, float partialTick) {
        this.animateSmooth(state, definition, ageInTicks, partialTick, 1.0F);
    }

    protected void animateSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks,
                                 float partialTick, float speed) {
        float factor = state.factor(partialTick);
        if (factor <= SmoothAnimationState.ACTIVE_THRESHOLD || !state.isStarted()) {
            return;
        }
        long timeMs = (long) (state.getTimeInMillis(ageInTicks) * speed);
        this.animation(definition).apply(timeMs, factor);
    }

    protected void animateUnblended(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks) {
        if (state.isStarted()) {
            this.animation(definition).apply(state.getTimeInMillis(ageInTicks), 1.0F);
        }
    }

    protected void animateIdleSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks,
                                     float partialTick, float limbSwingAmount) {
        this.animateIdleSmooth(state, definition, ageInTicks, partialTick, limbSwingAmount, IDLE_FADE_SCALE, 1.0F);
    }

    protected void animateIdleSmooth(SmoothAnimationState state, AnimationDefinition definition, float ageInTicks,
                                     float partialTick, float limbSwingAmount, float animationScaleFactor, float speed) {
        float factor = state.factor(partialTick) * (1.0F - Math.min(limbSwingAmount * animationScaleFactor, 1.0F));
        if (factor <= SmoothAnimationState.ACTIVE_THRESHOLD || !state.isStarted()) {
            return;
        }
        long timeMs = (long) (state.getTimeInMillis(ageInTicks) * speed);
        this.animation(definition).apply(timeMs, factor);
    }
}
