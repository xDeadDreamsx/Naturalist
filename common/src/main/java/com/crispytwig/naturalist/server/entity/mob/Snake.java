package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.PetTargeting;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import com.crispytwig.naturalist.server.entity.base.TamableClimbingAnimal;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SearchForItemsGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SleepGoal;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class Snake extends TamableClimbingAnimal implements SleepingAnimal, NeutralMob, NaturalistGeoEntity, FollowingPet, HuntingAnimal {
    //region Data
    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.SNAKE_TEMPT_ITEMS);
    private static final Ingredient TAME_ITEMS = Ingredient.of(NaturalistTags.ItemTags.SNAKE_TAME_ITEMS);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private static final EntityDataAccessor<Integer> REMAINING_ANGER_TIME = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.INT);

    private boolean followingOwner = true;
    private int huntingCooldown;
    @Nullable
    private UUID persistentAngerTarget;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation MOVE = RawAnimation.begin().thenPlay("animation.sf_nba.snake.move");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.sf_nba.snake.sleep");
    protected static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("animation.sf_nba.snake.climb");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.snake.attack");
    protected static final RawAnimation TONGUE = RawAnimation.begin().thenPlay("animation.sf_nba.snake.tongue");
    protected static final RawAnimation RATTLE = RawAnimation.begin().thenLoop("animation.sf_nba.snake.rattle");

    public Snake(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
        builder.define(EAT_COUNTER, 0);
        builder.define(REMAINING_ANGER_TIME, 0);
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

    public boolean isEating() {
        return this.entityData.get(EAT_COUNTER) > 0;
    }

    public void eat(boolean eat) {
        this.entityData.set(EAT_COUNTER, eat ? 1 : 0);
    }

    private int getEatCounter() {
        return this.entityData.get(EAT_COUNTER);
    }

    private void setEatCounter(int amount) {
        this.entityData.set(EAT_COUNTER, amount);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(REMAINING_ANGER_TIME, time);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(REMAINING_ANGER_TIME);
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.addPersistentAngerSaveData(compound);
        FollowingPet.save(this, compound);
        this.addHuntingCooldownSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readPersistentAngerSaveData(this.level(), compound);
        FollowingPet.load(this, compound);
        this.readHuntingCooldownSaveData(compound);
    }
    //endregion

    //region Spawning
    public static boolean checkSnakeSpawnRules(EntityType<Snake> entityType, LevelAccessor level, MobSpawnType type, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.DIRT) && isBrightEnoughToSpawn(level, pos);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        this.populateDefaultEquipmentSlots(random, difficulty);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource random, @NotNull DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.2F) {
            float chance = random.nextFloat();
            ItemStack stack;
            if (chance < 0.05F) {
                stack = new ItemStack(Items.RABBIT_FOOT);
            } else if (chance < 0.1F) {
                stack = new ItemStack(Items.SLIME_BALL);
            } else if (chance < 0.15F) {
                stack = new ItemStack(Items.FEATHER);
            } else if (chance < 0.3F) {
                stack = new ItemStack(Items.RABBIT);
            } else {
                stack = new ItemStack(Items.CHICKEN);
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    public boolean isTameFood(ItemStack stack) {
        return TAME_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new SnakeMeleeAttackGoal(this, 1.75D, true));
        this.goalSelector.addGoal(2, new SearchForItemsGoal(this, 1.2F, FOOD_ITEMS, 8.0D, 8.0D));
        this.goalSelector.addGoal(3, new SleepGoal<>(this));
        this.goalSelector.addGoal(4, new PetFollowOwnerGoal(this, 1.2D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false, livingEntity -> this.hasHuntingCooldown() && (livingEntity.getType().is(NaturalistTags.EntityTypes.SNAKE_HOSTILES) || (livingEntity instanceof Slime slime && slime.isTiny()))));
        this.targetSelector.addGoal(6, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public boolean wantsToAttack(@NotNull LivingEntity target, @NotNull LivingEntity owner) {
        return PetTargeting.wantsToAttack(target, owner);
    }

    @Override
    protected float getClimbSpeedMultiplier() {
        return 0.5F;
    }

    @Override
    public float getSpeed() {
        return this.getMainHandItem().isEmpty() ? super.getSpeed() : super.getSpeed() * 0.5F;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!this.getMainHandItem().isEmpty() && !this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, this.getMainHandItem());
            itemEntity.setPickUpDelay(80);
            itemEntity.setThrower(this);
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            this.level().addFreshEntity(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        if ((this.getType().equals(NaturalistEntityTypes.CORAL_SNAKE.get()) || this.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get())) && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
        }
        return super.doHurtTarget(entity);
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        return !PetTargeting.protectsOwnedPet(this, target) && super.canAttack(target);
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
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
        if (this.level().isClientSide) {
            boolean canInteract = this.isOwnedBy(player) || this.isTame() || (this.isTameFood(stack) && !this.isTame());
            return canInteract ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (this.isTameFood(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(4.0F);
                return InteractionResult.SUCCESS;
            }
            if (this.isOwnedBy(player) && !this.isTameFood(stack)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
        if (this.isTameFood(stack)) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (this.random.nextInt(2) == 0) {
                this.tame(player);
                this.setOrderedToSit(true);
                this.navigation.stop();
                this.setTarget(null);
                this.stopBeingAngry();
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        EquipmentSlot slot = getEquipmentSlotForItem(itemStack);
        if (!this.getItemBySlot(slot).isEmpty()) {
            return false;
        } else {
            return slot == EquipmentSlot.MAINHAND && super.canTakeItem(itemStack);
        }
    }

    @Override
    protected void pickUpItem(@NotNull ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (this.getMainHandItem().isEmpty() && FOOD_ITEMS.test(stack)) {
            this.onItemPickup(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(itemEntity, stack.getCount());
            itemEntity.discard();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
            this.tickHuntingCooldown();
        }
        if (this.isSleeping() || this.isImmobile() || this.isInSittingPose()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
        this.handleEating();
        if (!this.getMainHandItem().isEmpty()) {
            if (this.isAngry()) {
                this.stopBeingAngry();
            }
        }
        if (this.canRattle() && !this.isSleeping()) {
            this.playSound(NaturalistSoundEvents.SNAKE_RATTLE.get(), 0.15F, 1.0F);
        }
    }

    private void handleEating() {
        if (!this.isEating() && !this.isSleeping() && !this.getMainHandItem().isEmpty()) {
            this.eat(true);
        } else if (this.getMainHandItem().isEmpty()) {
            this.eat(false);
        }
        if (this.isEating()) {
            if (!this.level().isClientSide && this.getEatCounter() > 6000) {
                if (!this.getMainHandItem().isEmpty()) {
                    if (!this.level().isClientSide) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.gameEvent(GameEvent.EAT);
                    }
                }
                this.eat(false);
                return;
            }
            this.setEatCounter(this.getEatCounter() + 1);
        }
    }

    @Override
    public boolean canSleep() {
        long dayTime = this.level().getDayTime();
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else if (dayTime > 18000 && dayTime < 23000) {
            return false;
        } else return dayTime > 12000 && dayTime < 28000;
    }

    private boolean canRattle() {
        List<Player> players = this.level().getNearbyPlayers(TargetingConditions.forNonCombat().range(4.0D), this, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
        if(!players.isEmpty() && this.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get()) && !players.getFirst().isCreative()){
            this.setTarget(players.getFirst());
        } else {
            this.setTarget(null);
        }
        return !players.isEmpty() && this.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get());
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.SNAKE_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.SNAKE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.SNAKE_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.15F;
    }

    static class SnakeMeleeAttackGoal extends MeleeAttackGoal {
        private long lastCanUseCheck;

        public SnakeMeleeAttackGoal(@NotNull PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return mob.getMainHandItem().isEmpty() && testUse();
        }

        boolean testUse(){
            long l = this.mob.level().getGameTime();
            if (l - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = l;
                LivingEntity livingEntity = this.mob.getTarget();
                if (livingEntity == null) {
                    return false;
                } else if (!livingEntity.isAlive()) {
                    return false;
                } else {
                    Path path = this.mob.getNavigation().createPath(livingEntity, 0);
                    if (path != null) {
                        return true;
                    } else {
                        return this.getAttackReachSqr(livingEntity) >= this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getMainHandItem().isEmpty() && super.canContinueToUse();
        }

        protected double getAttackReachSqr(LivingEntity attackTarget) {
            return 4.0F + attackTarget.getBbWidth();
        }
    }
    //endregion

    //region Animation
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Snake> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (this.isSleeping() || this.isInSittingPose()) {
            event.getController().setAnimation(SLEEP);
            return PlayState.CONTINUE;
        } else if (this.isNaturalistClimbing()) {
            event.getController().setAnimation(CLIMB);
            return PlayState.CONTINUE;
        } else if (!(event.getLimbSwingAmount() > -0.04F && event.getLimbSwingAmount() < 0.04F)) {
            event.getController().setAnimation(MOVE);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    private <E extends Snake> PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends Snake> @NotNull PlayState tonguePredicate(final AnimationState<E> event) {
        if (this.random.nextInt(1000) < this.ambientSoundTime && !this.isSleeping() && event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.getController().forceAnimationReset();

            event.getController().setAnimation(TONGUE);
        }
        return PlayState.CONTINUE;
    }

    private <E extends Snake> @NotNull PlayState rattlePredicate(final AnimationState<E> event) {
        if (this.canRattle() && !this.isSleeping()) {
            event.getController().setAnimation(RATTLE);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Snake> event) {
        Snake snake = event.getAnimatable();
        if (snake.level().isClientSide) {
            if (event.getKeyframeData().getSound().equals("hiss")) {
                snake.level().playLocalSound(snake.getX(), snake.getY(), snake.getZ(), NaturalistSoundEvents.SNAKE_HISS.get(), snake.getSoundSource(), snake.getSoundVolume(), snake.getVoicePitch(), false);
            }
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 10, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));

        AnimationController<Snake> tongueController = new AnimationController<>(this, "tongueController", 0, this::tonguePredicate);
        tongueController.setSoundKeyframeHandler(this::soundListener);
        controllers.add(tongueController);
        controllers.add(new AnimationController<>(this, "rattleController", 0, this::rattlePredicate));
    }
    //endregion
}
