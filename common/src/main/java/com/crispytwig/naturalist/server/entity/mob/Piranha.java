package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.crispytwig.naturalist.server.entity.util.SmoothSpeedAnimationController;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("unused")
public class Piranha extends AbstractSchoolingFish implements NaturalistGeoEntity, DataDrivenVariantAnimal {
    //region Data
    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Piranha.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TARGET = SynchedEntityData.defineId(Piranha.class, EntityDataSerializers.BOOLEAN);

    public float xBodyRot;
    public float xBodyRotO;

    @Nullable
    private AbstractSchoolingFish schoolLeader;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.piranha.swim");
    protected static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("animation.sf_nba.piranha.swim_fast");
    protected static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.piranha.flop");

    public Piranha(EntityType<? extends AbstractSchoolingFish> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0D).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.MOVEMENT_SPEED, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, this.defaultVariant().location().toString());
        builder.define(DATA_HAS_TARGET, false);
    }

    public boolean hasSwimTarget() {
        return this.entityData.get(DATA_HAS_TARGET);
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/piranha.png");
    }

    @Override
    public String getVariantRawId() {
        return this.entityData.get(DATA_VARIANT);
    }

    @Override
    public void setVariantRawId(String id) {
        this.entityData.set(DATA_VARIANT, id);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveVariant(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.loadVariant(compound);
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NaturalistRegistry.PIRANHA_BUCKET.get());
    }
    //endregion

    //region Spawning
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType != MobSpawnType.BUCKET) {
            this.pickVariantForSpawn(level);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4D, true));
        this.goalSelector.addGoal(2, new FollowFlockLeaderGoal(this));
        this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1.0D, 10));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, LivingEntity::isInWater) {
            @Override
            public boolean canUse() {
                return !Piranha.this.isFollower() && super.canUse();
            }
        });
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, WaterAnimal.class, 10, true, false, (entity) -> entity.getType().is(NaturalistTags.EntityTypes.PIRANHA_HOSTILES)) {
            @Override
            public boolean canUse() {
                return !Piranha.this.isFollower() && super.canUse();
            }
        });
    }

    @Override
    public @NotNull AbstractSchoolingFish startFollowing(@NotNull AbstractSchoolingFish leader) {
        this.schoolLeader = leader;
        return super.startFollowing(leader);
    }

    @Override
    public void stopFollowing() {
        super.stopFollowing();
        this.schoolLeader = null;
    }

    @Override
    public int getMaxSchoolSize() {
        return 24;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
            this.setTarget(attacker);
            if (this.schoolLeader instanceof Piranha leader && leader.isAlive()) {
                leader.setTarget(attacker);
            }
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
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.isFollower() && this.schoolLeader instanceof Piranha leader) {
                LivingEntity leaderTarget = leader.getTarget();
                if (leaderTarget != this.getTarget()) {
                    this.setTarget(leaderTarget);
                }
            }
            this.entityData.set(DATA_HAS_TARGET, this.getTarget() != null);
        }

        this.xBodyRotO = this.xBodyRot;
        Vec3 velocity = this.getDeltaMovement();
        float horizontal = (float) velocity.horizontalDistance();
        float targetPitch = 0.0F;
        if (this.isInWater() && horizontal > 0.01F) {
            targetPitch = -((float) (Mth.atan2(velocity.y, horizontal) * (180.0 / Math.PI)));
        }
        this.xBodyRot += (targetPitch - this.xBodyRot) * 0.2F;
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return NaturalistSoundEvents.PIRANHA_FLOP.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.PIRANHA_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.PIRANHA_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.PIRANHA_DEATH.get();
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getBoneResetTime() {
        return 5;
    }

    protected <E extends Piranha> @NotNull PlayState predicate(final AnimationState<E> event) {
        AnimationController<E> controller = event.getController();
        if (!this.isInWater()) {
            controller.setAnimation(FLOP);
            controller.setAnimationSpeed(1.0D);
        } else if (this.hasSwimTarget()) {
            controller.setAnimation(SWIM_FAST);
            controller.setAnimationSpeed(this.movementAnimationSpeed(event, 1.0D, LARGE_FISH_LIMB_SWING));
        } else {
            controller.setAnimation(SWIM);
            controller.setAnimationSpeed(this.movementAnimationSpeed(event, 1.0D, SMALL_FISH_LIMB_SWING));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new SmoothSpeedAnimationController<>(this, "controller", 5, this::predicate));
    }

    public float getXBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.xBodyRotO, this.xBodyRot);
    }
    //endregion
}
