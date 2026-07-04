package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SleepGoal;
import com.crispytwig.naturalist.server.entity.base.Catchable;
import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import com.crispytwig.naturalist.server.entity.base.TamableClimbingAnimal;
import com.crispytwig.naturalist.server.entity.base.VariantAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
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

import java.util.EnumSet;
import java.util.Optional;

public class Rat extends TamableClimbingAnimal implements NaturalistGeoEntity, SleepingAnimal, FollowingPet, Catchable, VariantAnimal {
    //region Data
    public static final String[] VARIANT_NAMES = {"black", "brown", "white"};

    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.RAT_FOOD);

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Rat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Rat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> INTERESTED = SynchedEntityData.defineId(Rat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_HAND = SynchedEntityData.defineId(Rat.class, EntityDataSerializers.BOOLEAN);

    private boolean followingOwner = true;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.rat.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.rat.walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.sf_nba.rat.run");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.rat.swim");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.sf_nba.rat.sleep");
    protected static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.sf_nba.rat.sit");
    protected static final RawAnimation STANDING = RawAnimation.begin().thenLoop("animation.sf_nba.rat.standing");

    public Rat(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(SLEEPING, false);
        builder.define(INTERESTED, false);
        builder.define(FROM_HAND, false);
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
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean isInterested() {
        return this.entityData.get(INTERESTED);
    }

    public void setInterested(boolean interested) {
        this.entityData.set(INTERESTED, interested);
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
    public boolean fromHand() {
        return this.entityData.get(FROM_HAND);
    }

    @Override
    public void setFromHand(boolean fromHand) {
        this.entityData.set(FROM_HAND, fromHand);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromHand();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(VARIANT_TAG, this.getVariant());
        compound.putBoolean("FromHand", this.fromHand());
        FollowingPet.save(this, compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt(VARIANT_TAG));
        this.setFromHand(compound.getBoolean("FromHand"));
        FollowingPet.load(this, compound);
    }

    @Override
    public void saveToHandTag(@NotNull ItemStack stack) {
        Catchable.saveDefaultDataToHandTag(this, stack);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt(VARIANT_TAG, this.getVariant());
        tag.putInt("Age", this.getAge());
        Catchable.saveTamableDataToHandTag(this, tag);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public void loadFromHandTag(@NotNull CompoundTag tag) {
        Catchable.loadDefaultDataFromHandTag(this, tag);
        this.setVariant(tag.getInt(VARIANT_TAG));
        this.setAge(tag.getInt("Age"));
        Catchable.loadTamableDataFromHandTag(this, tag);
    }

    @Override
    public ItemStack getCaughtItemStack() {
        return new ItemStack(NaturalistRegistry.RAT.get());
    }

    @Nullable
    @Override
    public SoundEvent getPickupSound() {
        return null;
    }
    //endregion

    //region Spawning
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        Rat baby = NaturalistEntityTypes.RAT.get().create(level);
        if (baby != null) {
            int variant = this.random.nextBoolean() || !(mob instanceof Rat other) ? this.getVariant() : other.getVariant();
            baby.setVariant(variant);
            if (this.isTame()) {
                baby.setOwnerUUID(this.getOwnerUUID());
                baby.setTame(true, true);
            }
        }
        return baby;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnType != MobSpawnType.BUCKET) {
            this.setVariant(this.random.nextInt(this.getVariantCount()));
        }
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.05F);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new SleepGoal<>(this));
        this.goalSelector.addGoal(4, new RatBegGoal(this));
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(7, new PetFollowOwnerGoal(this, 1.4D, 10.0F, 3.0F) {
            @Override
            public void start() {
                super.start();
                Rat.this.setSprinting(true);
            }

            @Override
            public void stop() {
                super.stop();
                Rat.this.setSprinting(false);
            }
        });
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F, 0.02F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean canSleep() {
        if (this.level().isDay() || this.level().isThundering() || this.isInWater() || this.isOnFire()
                || !this.onGround() || this.isOrderedToSit() || this.isPassenger()) {
            return false;
        }
        return this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(12.0D, 6.0D, 12.0D),
                mob -> mob != this && !(mob instanceof Rat)).isEmpty();
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        InteractionResult whistle = FollowingPet.tryWhistle(this, player, hand);
        if (whistle != null) {
            return whistle;
        }
        if (this.isTame()) {
            Optional<InteractionResult> caught = Catchable.catchAnimal(player, hand, this, true);
            if (caught.isPresent()) {
                return caught.get();
            }
        }
        ItemStack stack = player.getItemInHand(hand);
        if (this.isFood(stack)) {
            if (!this.isTame()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (!this.level().isClientSide) {
                    if (this.random.nextInt(10) < 7) {
                        this.tame(player);
                        this.setOrderedToSit(true);
                        this.navigation.stop();
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide) {
                    this.usePlayerItem(player, hand, stack);
                    this.heal(2.0F);
                    this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            return super.mobInteract(player, hand);
        }
        if (this.isTame() && this.isOwnedBy(player) && !player.isSecondaryUseActive()) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSleeping() ? null : NaturalistSoundEvents.RAT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return NaturalistSoundEvents.RAT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.RAT_DEATH.get();
    }

    @Override
    protected @NotNull SoundEvent getSwimSound() {
        return NaturalistSoundEvents.RAT_SWIM.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
    }

    private static class RatBegGoal extends Goal {
        private final Rat rat;
        private final TargetingConditions begTargeting;
        @Nullable
        private Player player;
        private int lookTime;

        public RatBegGoal(Rat rat) {
            this.rat = rat;
            this.begTargeting = TargetingConditions.forNonCombat().range(4.0D);
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.player = this.rat.level().getNearestPlayer(this.begTargeting, this.rat);
            return this.player != null && this.playerHoldingFood(this.player);
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null && this.player.isAlive() && this.lookTime > 0
                    && this.rat.distanceToSqr(this.player) <= 25.0D && this.playerHoldingFood(this.player);
        }

        @Override
        public void start() {
            this.rat.setInterested(true);
            this.rat.getNavigation().stop();
            this.lookTime = this.adjustedTickDelay(40 + this.rat.getRandom().nextInt(40));
        }

        @Override
        public void stop() {
            this.rat.setInterested(false);
            this.player = null;
        }

        @Override
        public void tick() {
            if (this.player != null) {
                this.rat.getLookControl().setLookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ(), 10.0F, this.rat.getMaxHeadXRot());
            }
            this.lookTime--;
        }

        private boolean playerHoldingFood(Player player) {
            for (InteractionHand hand : InteractionHand.values()) {
                if (this.rat.isFood(player.getItemInHand(hand))) {
                    return true;
                }
            }
            return false;
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Rat> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (this.isInSittingPose()) {
            event.getController().setAnimation(SIT);
        } else if (this.isSleeping()) {
            event.getController().setAnimation(SLEEP);
        } else if (this.isInWater()) {
            event.getController().setAnimation(SWIM);
        } else if (event.isMoving()) {
            if (this.isSprinting() || this.getDeltaMovement().horizontalDistanceSqr() > 0.01D) {
                event.getController().setAnimation(RUN);
                event.getController().setAnimationSpeed(1.0D);
            } else {
                event.getController().setAnimation(WALK);
                event.getController().setAnimationSpeed(1.5D);
            }
        } else if (this.isInterested()) {
            event.getController().setAnimation(STANDING);
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Rat> event) {
        Rat rat = event.getAnimatable();
        if (!rat.level().isClientSide) {
            return;
        }
        float volume;
        switch (event.getKeyframeData().getSound()) {
            case "step_-6dB" -> volume = 0.25F;
            case "step_-12dB" -> volume = 0.15F;
            default -> {
                return;
            }
        }
        rat.level().playLocalSound(rat.getX(), rat.getY(), rat.getZ(), NaturalistSoundEvents.RAT_STEP.get(), rat.getSoundSource(), volume, 1.0F, false);
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, this::predicate)
                .setSoundKeyframeHandler(this::soundListener));
    }
    //endregion
}
