package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("unused")
public class Ray extends AbstractFish implements NaturalistGeoEntity {
    //region Data
    public static final int VARIANTS = 3;
    public static final String[] VARIANT_NAMES = {"eagle_ray", "mobula_ray", "stingray"};

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Ray.class, EntityDataSerializers.INT);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final float MAX_TILT = 22.0F;
    private static final float MAX_ROLL = 25.0F;
    private static final float ROLL_PER_YAW = 2.0F;

    public float xBodyRot;
    public float xBodyRotO;
    public float finLag;
    public float finLagO;
    public float tailLag;
    public float tailLagO;
    public float zBodyRot;
    public float zBodyRotO;
    private float lastYRot;
    private Vec3 lastMoveDir = Vec3.ZERO;

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.ray.idle");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.ray.swim_idle_event");

    public Ray(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 14.0D).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    public String getVariantName() {
        return VARIANT_NAMES[Math.floorMod(this.getVariant(), VARIANTS)];
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt("Variant"));
    }

    @Override
    public void saveToBucketTag(@NotNull ItemStack stack) {
        super.saveToBucketTag(stack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> tag.putInt("Variant", this.getVariant()));
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        custom.putInt("Variant", this.getVariant());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag tag) {
        super.loadFromBucketTag(tag);
        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        }
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NaturalistRegistry.RAY_BUCKET.get());
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return NaturalistSoundEvents.CATFISH_FLOP.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.RAY_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.RAY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.RAY_DEATH.get();
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0D, 20));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.setTarget(attacker);
        }
        return hurt;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && !this.level().isClientSide && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), this);
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.xBodyRotO = this.xBodyRot;
        this.finLagO = this.finLag;
        this.tailLagO = this.tailLag;
        float target = 0.0F;
        if (this.isInWater()) {
            Vec3 movement = this.getDeltaMovement();
            if (movement.lengthSqr() > 1.0E-6) {
                this.lastMoveDir = movement;
            }
            target = -((float) Mth.atan2(this.lastMoveDir.y, this.lastMoveDir.horizontalDistance()) * (180.0F / (float) Math.PI));
            target = Mth.clamp(target, -MAX_TILT, MAX_TILT);
        }
        this.xBodyRot += (target - this.xBodyRot) * 0.2F;
        this.finLag += (this.xBodyRot - this.finLag) * 0.15F;
        this.tailLag += (this.xBodyRot - this.tailLag) * 0.09F;

        this.zBodyRotO = this.zBodyRot;
        float yawDelta = Mth.wrapDegrees(this.getYRot() - this.lastYRot);
        this.lastYRot = this.getYRot();
        float rollTarget = this.isInWater() ? Mth.clamp(-yawDelta * ROLL_PER_YAW, -MAX_ROLL, MAX_ROLL) : 0.0F;
        this.zBodyRot += (rollTarget - this.zBodyRot) * 0.2F;
    }

    public float getXBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.xBodyRotO, this.xBodyRot);
    }

    public float getZBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.zBodyRotO, this.zBodyRot);
    }

    public float getFinLag(float partialTick) {
        return Mth.lerp(partialTick, this.finLagO, this.finLag);
    }

    public float getTailLag(float partialTick) {
        return Mth.lerp(partialTick, this.tailLagO, this.tailLag);
    }
    //endregion

    //region Animation
    @Override
    public double getBoneResetTime() {
        return 5;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Ray> @NotNull PlayState predicate(final AnimationState<E> event) {
        AnimationController<E> controller = event.getController();
        controller.setAnimation(this.isInWater() ? SWIM : IDLE);
        return PlayState.CONTINUE;
    }

    private void soundListener(SoundKeyframeEvent<Ray> event) {
        Ray ray = event.getAnimatable();
        if (ray.level().isClientSide && "swim".equals(event.getKeyframeData().getSound())) {
            ray.level().playLocalSound(ray.getX(), ray.getY(), ray.getZ(), NaturalistSoundEvents.RAY_SWIM.get(), ray.getSoundSource(), 0.3F, 1.0F, false);
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate).setSoundKeyframeHandler(this::soundListener));
    }
    //endregion
}
