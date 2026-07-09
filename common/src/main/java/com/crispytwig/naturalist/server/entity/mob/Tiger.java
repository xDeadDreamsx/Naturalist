package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistMobVariants;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.ai.goal.BabyHurtByTargetGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.BabyPanicGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SleepGoal;
import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.base.NocturnalHostile;
import com.crispytwig.naturalist.server.entity.base.PetTargeting;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.entity.variant.MobVariant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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
import java.util.List;
import java.util.Objects;

public class Tiger extends TamableAnimal implements NaturalistGeoEntity, SleepingAnimal, FollowingPet, HuntingAnimal, DataDrivenVariantAnimal, NocturnalHostile {
    //region Data
    public static final String[] VARIANT_NAMES = {"black_panther", "leopard", "tiger", "white_tiger"};

    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.TIGER_FOOD_ITEMS);

    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Tiger.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Tiger.class, EntityDataSerializers.BOOLEAN);

    private boolean followingOwner = true;
    private int huntingCooldown;
    private boolean stalking;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.tiger.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.tiger.walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.sf_nba.tiger.run");
    protected static final RawAnimation PREY = RawAnimation.begin().thenLoop("animation.sf_nba.tiger.prey");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.tiger.attack");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.sf_nba.tiger.sleep");
    protected static final RawAnimation SLEEP2 = RawAnimation.begin().thenLoop("animation.sf_nba.tiger.sleep2");

    public Tiger(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, NaturalistMobVariants.TIGER_BLACK_PANTHER.location().toString());
        builder.define(SLEEPING, false);
    }

    @Override
    public ResourceKey<MobVariant> defaultVariant() {
        return NaturalistMobVariants.TIGER_BLACK_PANTHER;
    }

    @Override
    public String[] legacyVariantNames() {
        return VARIANT_NAMES;
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/tiger/tiger.png");
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
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean usesAltSleepPose() {
        return (this.getUUID().hashCode() & 1) == 0;
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
    public void setHuntingCooldown(int huntingCooldown) {
        this.huntingCooldown = huntingCooldown;
    }

    public boolean isStalking() {
        return this.stalking;
    }

    public void setStalking(boolean stalking) {
        this.stalking = stalking;
    }

    public boolean isPackLeader() {
        LivingEntity target = this.getTarget();
        return !this.isBaby() && !this.isTame() && target != null && target.isAlive() && !(target instanceof Tiger);
    }

    public boolean isCharging() {
        return this.isSprinting();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveVariant(compound);
        FollowingPet.save(this, compound);
        this.addHuntingCooldownSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.loadVariant(compound);
        FollowingPet.load(this, compound);
        this.readHuntingCooldownSaveData(compound);
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
        Tiger baby = NaturalistEntityTypes.TIGER.get().create(level);
        if (baby != null) {
            baby.setVariantRawId(this.inheritVariantFrom(mob, this.random));
        }
        return baby;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.05F);
        }
        this.pickVariantForSpawn(level);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
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
        this.goalSelector.addGoal(2, new TigerFollowLeaderGoal(this, 24.0F));
        this.goalSelector.addGoal(3, new SleepGoal<>(this));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TigerFollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new PetFollowOwnerGoal(this, 1.3D, 7.0F, 2.0F));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new BabyHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(4, new HurtByTargetGoal(this).setAlertOthers(Tiger.class));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, true,
                entity -> entity.getType().is(NaturalistTags.EntityTypes.TIGER_HOSTILES) && !entity.isBaby()
                        && !this.isSleeping() && !this.isBaby() && !this.isStalking() && this.isNightTime() && this.hasHuntingCooldown()));
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
        this.setPose(this.isStalking() ? Pose.CROUCHING : Pose.STANDING);
        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 1.05D && this.onGround());
        } else {
            this.setSprinting(false);
        }
    }

    @Override
    public boolean canSleep() {
        long dayTime = this.level().getDayTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return dayTime > 6000 && dayTime < 13000;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.TIGER_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.TIGER_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.TIGER_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 900;
    }

    static class TigerFollowParentGoal extends FollowParentGoal {
        private final Tiger tiger;

        public TigerFollowParentGoal(Tiger tiger, double speedModifier) {
            super(tiger, speedModifier);
            this.tiger = tiger;
        }

        @Override
        public boolean canUse() {
            return !this.tiger.isSleeping() && !this.tiger.isTame() && super.canUse();
        }
    }

    static class TigerFollowLeaderGoal extends Goal {
        private final Tiger tiger;
        private final float areaSize;
        @Nullable
        private Tiger leader;
        private int timeToRecalcPath;

        public TigerFollowLeaderGoal(Tiger tiger, float areaSize) {
            this.tiger = tiger;
            this.areaSize = areaSize;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.tiger.isBaby() || this.tiger.isTame() || this.tiger.getTarget() != null || !this.tiger.hasHuntingCooldown()) {
                return false;
            }
            if (this.tiger.tickCount % 10 != 0) {
                return false;
            }
            List<Tiger> nearby = this.tiger.level().getEntitiesOfClass(Tiger.class, this.tiger.getBoundingBox().inflate(this.areaSize),
                    other -> other != this.tiger && other.isPackLeader());
            if (nearby.isEmpty()) {
                return false;
            }
            this.leader = nearby.getFirst();
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.leader != null && this.leader.isAlive() && this.leader.isPackLeader() && this.tiger.getTarget() == null;
        }

        @Override
        public void start() {
            this.tiger.setStalking(true);
            this.timeToRecalcPath = 0;
        }

        @Override
        public void stop() {
            this.tiger.setStalking(false);
            this.leader = null;
            this.tiger.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.leader == null) {
                return;
            }
            this.tiger.getLookControl().setLookAt(this.leader, 10.0F, this.tiger.getMaxHeadXRot());
            if (this.leader.isCharging()) {
                this.tiger.setTarget(this.leader.getTarget());
                return;
            }
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = this.adjustedTickDelay(10);
                if (this.tiger.distanceToSqr(this.leader) >= 36.0D) {
                    this.tiger.getNavigation().moveTo(this.leader, 0.6D);
                } else {
                    this.tiger.getNavigation().stop();
                }
            }
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Tiger> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (this.isInSittingPose() || this.isSleeping()) {
            event.getController().setAnimation(this.usesAltSleepPose() ? SLEEP2 : SLEEP);
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

    protected <E extends Tiger> @NotNull PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Tiger> event) {
        Tiger tiger = event.getAnimatable();
        if (!tiger.level().isClientSide) {
            return;
        }
        SoundEvent sound;
        float volume;
        switch (event.getKeyframeData().getSound()) {
            case "attack" -> {
                sound = NaturalistSoundEvents.TIGER_ATTACK.get();
                volume = 1.0F;
            }
            case "sleep" -> {
                sound = NaturalistSoundEvents.TIGER_SLEEP.get();
                volume = 0.7F;
            }
            case "prey" -> {
                sound = NaturalistSoundEvents.TIGER_PREY.get();
                volume = 1.0F;
            }
            case "step" -> {
                sound = NaturalistSoundEvents.TIGER_STEP.get();
                volume = 0.4F;
            }
            case "step_-6dB" -> {
                sound = NaturalistSoundEvents.TIGER_STEP.get();
                volume = 0.25F;
            }
            default -> {
                return;
            }
        }
        tiger.level().playLocalSound(tiger.getX(), tiger.getY(), tiger.getZ(), sound, tiger.getSoundSource(), volume, 1.0F, false);
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate)
                .setSoundKeyframeHandler(this::soundListener));
    }
    //endregion
}
