package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.PetTargeting;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.NocturnalHostile;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import com.crispytwig.naturalist.server.entity.ai.goal.BabyHurtByTargetGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.BabyPanicGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SleepGoal;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Lion extends TamableAnimal implements NaturalistGeoEntity, SleepingAnimal, FollowingPet, HuntingAnimal, NocturnalHostile {
    //region Data
    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.LION_FOOD_ITEMS);
    private static final ResourceLocation BABY_SPEED_BOOST_ID = ResourceLocation.fromNamespaceAndPath("naturalist", "baby_speed_boost");
    private static final AttributeModifier BABY_SPEED_BOOST = new AttributeModifier(BABY_SPEED_BOOST_ID, 0.05D, AttributeModifier.Operation.ADD_VALUE);

    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Lion.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_MANE = SynchedEntityData.defineId(Lion.class, EntityDataSerializers.BOOLEAN);

    private boolean followingOwner = true;
    private int huntingCooldown;
    private boolean wasSitting;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.lion.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.lion.walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.sf_nba.lion.run");
    protected static final RawAnimation PREY = RawAnimation.begin().thenLoop("animation.sf_nba.lion.prey");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.lion.attack");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.sf_nba.lion.sleep");
    protected static final RawAnimation SLEEP2 = RawAnimation.begin().thenLoop("animation.sf_nba.lion.sleep2");
    protected static final RawAnimation SIT = RawAnimation.begin().thenPlay("animation.sf_nba.lion.sit").thenLoop("animation.sf_nba.lion.sit_idle");
    protected static final RawAnimation UNSIT = RawAnimation.begin().thenPlay("animation.sf_nba.lion.unsit");

    public Lion(@NotNull EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25F)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
        builder.define(HAS_MANE, false);
    }

    @Override
    public boolean isFollowingOwner() {
        return this.followingOwner;
    }

    @Override
    public void setFollowingOwner(boolean following) {
        this.followingOwner = following;
    }

    @Override
    public int getHuntingCooldown() {
        return this.huntingCooldown;
    }

    @Override
    public void setHuntingCooldown(int ticks) {
        this.huntingCooldown = ticks;
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public void setHasMane(boolean hasMane) {
        this.entityData.set(HAS_MANE, hasMane);
    }

    public boolean hasMane() {
        return this.entityData.get(HAS_MANE);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Mane", this.hasMane());
        FollowingPet.save(this, compound);
        this.addHuntingCooldownSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setHasMane(compound.getBoolean("Mane"));
        FollowingPet.load(this, compound);
        this.readHuntingCooldownSaveData(compound);
    }
    //endregion

    //region Spawning
    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        super.finalizeSpawn(level, difficulty, reason, spawnData);
        AgeableMobGroupData ageableMobGroupData;
        if (spawnData == null) {
            spawnData = new AgeableMobGroupData(true);
            this.setHasMane(this.getRandom().nextBoolean());
        }
        if ((ageableMobGroupData = (AgeableMobGroupData)spawnData).getGroupSize() > 2) {
            this.setAge(-24000);
        }
        ageableMobGroupData.increaseGroupSizeByOne();
        RandomSource random = level.getRandom();
        Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE)).addPermanentModifier(new AttributeModifier(ResourceLocation.fromNamespaceAndPath("naturalist", "random_spawn_bonus"), random.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        return spawnData;
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.LION.get().create(serverLevel);
    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        this.setHasMane(this.getRandom().nextBoolean());
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.6D, true));
        this.goalSelector.addGoal(2, new BabyPanicGoal(this, 1.25D));
        this.goalSelector.addGoal(3, new SleepGoal<>(this));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LionFollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(5, new PetFollowOwnerGoal(this, 1.3D, 7.0F, 2.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(6, new LionFollowLeaderGoal(this, 1.1D, 8.0F, 24.0F));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new BabyHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, true,
                entity -> entity.getType().is(NaturalistTags.EntityTypes.LION_HOSTILES) && !entity.isBaby()
                        && !this.isSleeping() && !this.isBaby() && this.isNightTime() && this.hasHuntingCooldown()));
    }

    @Override
    public boolean wantsToAttack(@NotNull LivingEntity target, @NotNull LivingEntity owner) {
        return PetTargeting.wantsToAttack(target, owner);
    }

    @Override
    public boolean isSteppingCarefully() {
        return this.isCrouching() || super.isSteppingCarefully();
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        return !PetTargeting.protectsOwnedPet(this, target) && super.canAttack(target);
    }

    @Override
    public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity killed) {
        boolean result = super.killedEntity(level, killed);
        if (result) {
            this.startHuntingCooldown();
        }
        return result;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult whistle = FollowingPet.tryWhistle(this, player, hand);
        if (whistle != null) {
            return whistle;
        }
        if (this.isTame() && this.isBaby() && this.isFood(stack)) {
            this.ageUp(getSpeedUpSecondsWhenFeeding(-this.getAge()), true);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.level().isClientSide) {
            boolean canInteract = this.isOwnedBy(player) || this.isTame()
                    || (this.isBaby() && this.isFood(stack) && !this.isTame());
            return canInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(stack.has(DataComponents.FOOD)
                        ? Objects.requireNonNull(stack.get(DataComponents.FOOD)).nutrition()
                        : 4.0F);
                return InteractionResult.SUCCESS;
            }
            InteractionResult interactionResult = super.mobInteract(player, hand);
            if ((!interactionResult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.SUCCESS;
            }
            return interactionResult;
        }
        if (this.isBaby() && this.isFood(stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.navigation.stop();
                this.setTarget(null);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
    }

    @Override
    public void customServerAiStep() {
        this.tickHuntingCooldown();
        this.updateBabySpeed();
        if (this.getMoveControl().hasWanted()) {
            double speedModifier = this.getMoveControl().getSpeedModifier();
            double sprintThreshold = 1.05D;

                if (speedModifier >= sprintThreshold && this.onGround()) {
                this.setPose(Pose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(Pose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
        }
    }

    private void updateBabySpeed() {
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        boolean hasBoost = speed.hasModifier(BABY_SPEED_BOOST_ID);
        if (this.isBaby()) {
            if (!hasBoost) {
                speed.addTransientModifier(BABY_SPEED_BOOST);
            }
        } else if (hasBoost) {
            speed.removeModifier(BABY_SPEED_BOOST_ID);
        }
    }

    @Override
    public boolean canSleep() {
        long dayTime = this.level().getDayTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else {
            return dayTime > 6000 && dayTime < 13000;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? NaturalistSoundEvents.LION_AMBIENT_BABY.get() : NaturalistSoundEvents.LION_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return this.isBaby() ? NaturalistSoundEvents.LION_HURT_BABY.get() : NaturalistSoundEvents.LION_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? NaturalistSoundEvents.LION_DEATH_BABY.get() : NaturalistSoundEvents.LION_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 900;
    }

    static class LionFollowLeaderGoal extends Goal {
        private final Lion mob;
        private final Predicate<Mob> followPredicate;
        @Nullable
        private Lion followingMob;
        private final double speedModifier;
        private final PathNavigation navigation;
        private int timeToRecalcPath;
        private final float stopDistance;
        private float oldWaterCost;
        private final float areaSize;

        public LionFollowLeaderGoal(Lion mob, double speedModifier, float stopDistance, float areaSize) {
            this.mob = mob;
            this.followPredicate = followingMob -> followingMob != null && !followingMob.isBaby();
            this.speedModifier = speedModifier;
            this.navigation = mob.getNavigation();
            this.stopDistance = stopDistance;
            this.areaSize = areaSize;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mob.isBaby() || this.mob.hasMane()) {
                return false;
            }
            List<Lion> nearbyLions = this.mob.level().getEntitiesOfClass(Lion.class, this.mob.getBoundingBox().inflate(this.areaSize), this.followPredicate);
            if (!nearbyLions.isEmpty()) {
                for (Lion lion : nearbyLions) {
                    if (!lion.hasMane()) continue;
                    if (lion.isInvisible()) continue;
                    this.followingMob = lion;
                    return true;
                }
                if (this.followingMob == null) {
                    for (Lion lion : nearbyLions) {
                        if (lion.isBaby()) continue;
                        if (lion.isInvisible()) continue;
                        this.followingMob = lion;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.followingMob != null && !this.navigation.isDone() && this.mob.distanceToSqr(this.followingMob) > (double)(this.stopDistance * this.stopDistance);
        }

        @Override
        public void start() {
            this.timeToRecalcPath = 0;
            this.oldWaterCost = this.mob.getPathfindingMalus(PathType.WATER);
            this.mob.setPathfindingMalus(PathType.WATER, 0.0f);
        }

        @Override
        public void stop() {
            this.followingMob = null;
            this.navigation.stop();
            this.mob.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
        }

        @Override
        public void tick() {
            double f;
            double e;
            if (this.followingMob == null || this.mob.isLeashed()) {
                return;
            }
            this.mob.getLookControl().setLookAt(this.followingMob, 10.0f, this.mob.getMaxHeadXRot());
            if (--this.timeToRecalcPath > 0) {
                return;
            }
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            double d = this.mob.getX() - this.followingMob.getX();
            double g = d * d + (e = this.mob.getY() - this.followingMob.getY()) * e + (f = this.mob.getZ() - this.followingMob.getZ()) * f;
            if (g <= (double)(this.stopDistance * this.stopDistance)) {
                this.navigation.stop();
                LookControl lookControl = this.followingMob.getLookControl();
                if (g <= (double)this.stopDistance || lookControl.getWantedX() == this.mob.getX() && lookControl.getWantedY() == this.mob.getY() && lookControl.getWantedZ() == this.mob.getZ()) {
                    double h = this.followingMob.getX() - this.mob.getX();
                    double i = this.followingMob.getZ() - this.mob.getZ();
                    this.navigation.moveTo(this.mob.getX() - h, this.mob.getY(), this.mob.getZ() - i, this.speedModifier);
                }
                return;
            }
            this.navigation.moveTo(this.followingMob, this.speedModifier);
        }
    }

    static class LionFollowParentGoal extends FollowParentGoal {
        private final Lion lion;

        public LionFollowParentGoal(Lion animal, double speedModifier) {
            super(animal, speedModifier);
            this.lion = animal;
        }

        @Override
        public boolean canUse() {
            return !this.lion.isSleeping() && !this.lion.isTame() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.lion.isSleeping() && !this.lion.isTame() && super.canContinueToUse();
        }
    }
    //endregion

    //region Animation
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Lion> PlayState predicate(final AnimationState<E> event) {
        if (this.isBaby() && (this.isInSittingPose() || this.wasSitting)) {
            if (this.isInSittingPose()) {
                event.getController().setAnimation(SIT);
                this.wasSitting = true;
            } else {
                event.getController().setAnimation(UNSIT);
                if (event.getController().hasAnimationFinished()) {
                    this.wasSitting = false;
                }
            }
            return PlayState.CONTINUE;
        }
        if (!this.isBaby() && this.isInSittingPose()) {
            event.getController().setAnimation(this.hasMane() ? SLEEP2 : SLEEP);
            return PlayState.CONTINUE;
        }
        if (this.isSleeping() && this.hasMane()) {
            event.getController().setAnimation(SLEEP2);
        } else if (this.isSleeping() && !this.hasMane()) {
            event.getController().setAnimation(SLEEP);
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            if (this.isSprinting()) {
                event.getController().setAnimation(RUN);
                event.getController().setAnimationSpeed(2.5F);
            } else if (this.isCrouching()) {
                event.getController().setAnimation(PREY);
                event.getController().setAnimationSpeed(0.8F);
            } else {
                event.getController().setAnimation(WALK);
                event.getController().setAnimationSpeed(1.0F);
            }
        } else {
            event.getController().setAnimation(IDLE);
            event.getController().setAnimationSpeed(1.0F);
        }
        return PlayState.CONTINUE;
    }

    private <E extends Lion> PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }
    //endregion
}
