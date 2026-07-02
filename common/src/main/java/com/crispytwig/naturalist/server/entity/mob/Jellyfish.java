package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.base.VariantAnimal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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

import java.util.List;

@SuppressWarnings("unused")
public class Jellyfish extends AbstractFish implements NaturalistGeoEntity, VariantAnimal {
    //region Data
    public static final int VARIANTS = 5;
    public static final String[] VARIANT_NAMES = {"white", "orange", "pink", "blue", "green"};

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Jellyfish.class, EntityDataSerializers.INT);

    private static final float STING_DAMAGE = 2.0F;
    private static final int PULSE_INTERVAL = 20;
    private static final double PULSE_FORCE = 0.18D;
    private int pulseCooldown;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public float xBodyRot;
    public float xBodyRotO;
    private Vec3 lastMoveDir = Vec3.ZERO;

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.jellyfish.idle");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.jellyfish.swim");
    protected static final RawAnimation LAND = RawAnimation.begin().thenLoop("animation.sf_nba.jellyfish.land");

    public Jellyfish(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0D).add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
    }

    @Override
    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    @Override
    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    @Override
    public String[] getVariantNames() {
        return VARIANT_NAMES;
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
        return new ItemStack(NaturalistRegistry.JELLYFISH_BUCKET.get());
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.JELLYFISH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.JELLYFISH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.JELLYFISH_DEATH.get();
    }
    //endregion

    //region Spawning
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        if (reason != MobSpawnType.BUCKET) {
            this.setVariant(this.random.nextInt(VARIANTS));
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RandomSwimmingGoal(this, 1.0D, 20));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isAlive()) {
            List<LivingEntity> touching = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2D),
                    entity -> entity.isAlive() && !(entity instanceof Jellyfish));
            for (LivingEntity target : touching) {
                target.hurt(this.damageSources().mobAttack(this), STING_DAMAGE);
            }

            if (this.pulseCooldown > 0) {
                this.pulseCooldown--;
            }
            if (this.isInWater() && this.pulseCooldown <= 0 && this.getMoveControl().hasWanted()) {
                Vec3 toTarget = new Vec3(this.getMoveControl().getWantedX() - this.getX(),
                        this.getMoveControl().getWantedY() - this.getY(),
                        this.getMoveControl().getWantedZ() - this.getZ());
                if (toTarget.lengthSqr() > 1.0E-4) {
                    this.setDeltaMovement(this.getDeltaMovement().add(toTarget.normalize().scale(PULSE_FORCE)));
                }
                this.pulseCooldown = PULSE_INTERVAL;
            }
        }

        this.xBodyRotO = this.xBodyRot;
        float target = 0.0F;
        if (this.isInWater()) {
            Vec3 movement = this.getDeltaMovement();
            if (movement.lengthSqr() > 1.0E-6) {
                this.lastMoveDir = movement;
            }
            target = -((float) Mth.atan2(this.lastMoveDir.horizontalDistance(), this.lastMoveDir.y)) * (180.0F / (float) Math.PI);
        }
        this.xBodyRot += (target - this.xBodyRot) * 0.1F;
    }

    public float getXBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.xBodyRotO, this.xBodyRot);
    }
    //endregion

    //region Animation
    @Override
    public double getBoneResetTime() {
        return 2;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Jellyfish> @NotNull PlayState predicate(final AnimationState<E> event) {
        AnimationController<E> controller = event.getController();
        if (!this.isInWater()) {
            controller.setAnimation(LAND);
        } else if (this.getDeltaMovement().lengthSqr() > 1.0E-5) {
            controller.setAnimation(SWIM);
        } else {
            controller.setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(SoundKeyframeEvent<Jellyfish> event) {
        Jellyfish jellyfish = event.getAnimatable();
        if (jellyfish.level().isClientSide && "swim".equals(event.getKeyframeData().getSound())) {
            jellyfish.level().playLocalSound(jellyfish.getX(), jellyfish.getY(), jellyfish.getZ(), NaturalistSoundEvents.JELLYFISH_SWIM.get(), jellyfish.getSoundSource(), 0.4F, 1.0F, false);
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate).setSoundKeyframeHandler(this::soundListener));
    }
    //endregion
}
