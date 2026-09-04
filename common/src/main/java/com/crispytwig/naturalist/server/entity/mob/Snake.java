package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.PetTargeting;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import com.crispytwig.naturalist.server.entity.base.TamableClimbingAnimal;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SearchForItemsGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SleepGoal;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
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
import net.minecraft.world.entity.monster.cubemob.Slime;
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
import org.jetbrains.annotations.NotNull;
import com.crispytwig.naturalist.server.entity.util.AnimationTimer;
import com.crispytwig.naturalist.server.entity.util.SmoothAnimationState;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.core.registries.BuiltInRegistries;

@SuppressWarnings("unused")
public class Snake extends TamableClimbingAnimal implements SleepingAnimal, NeutralMob, FollowingPet, HuntingAnimal, DataDrivenVariantAnimal {
    //region Data
    private static Ingredient foodItems() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(NaturalistTags.ItemTags.SNAKE_TEMPT_ITEMS));
    }
    private static Ingredient tameItems() {
        return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(NaturalistTags.ItemTags.SNAKE_TAME_ITEMS));
    }
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> REMAINING_ANGER_TIME = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.INT);

    private boolean followingOwner = true;
    private int huntingCooldown;
    @Nullable
    private EntityReference<LivingEntity> persistentAngerTarget;
    private long persistentAngerEndTime = -1L;

    private int tongueTicks;

    public final SmoothAnimationState moveAnimationState = new SmoothAnimationState();
    public final SmoothAnimationState climbAnimationState = new SmoothAnimationState();
    public final SmoothAnimationState sleepAnimationState = SmoothAnimationState.pose();
    public final SmoothAnimationState attackAnimationState = SmoothAnimationState.instant();
    private final AnimationTimer attackAnimTimer = new AnimationTimer(10);
    public final SmoothAnimationState tongueAnimationState = SmoothAnimationState.instant();
    public final SmoothAnimationState rattleAnimationState = new SmoothAnimationState();

    public Snake(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 12.0D).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, this.getDefaultVariant().identifier().toString());
        builder.define(SLEEPING, false);
        builder.define(EAT_COUNTER, 0);
        builder.define(REMAINING_ANGER_TIME, 0);
    }

    @Override
    public Identifier getFallbackVariantTexture() {
        return Naturalist.location("textures/entity/snake/green_snake.png");
    }

    public boolean isVenomous() {
        return this.isRattlesnake() || this.getVariantLocation().getPath().equals("coral_snake");
    }

    public boolean isRattlesnake() {
        return this.getVariantLocation().getPath().equals("rattlesnake");
    }

    @Override
    public String getVariantString() {
        return this.entityData.get(DATA_VARIANT);
    }

    @Override
    public void setVariantString(String location) {
        this.entityData.set(DATA_VARIANT, location);
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

    public void setEating(boolean eating) {
        this.entityData.set(EAT_COUNTER, eating ? 1 : 0);
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
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity killed, @NotNull DamageSource source) {
        boolean result = super.killedEntity(level, killed, source);
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
        if (this.level().isClientSide()) {
            return (this.isOwnedBy(player) || this.isTame() || (tameItems().test(stack) && !this.isTame())) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (tameItems().test(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(4.0F);
                return InteractionResult.SUCCESS;
            }
            if (this.isOwnedBy(player) && !tameItems().test(stack)) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                this.setTarget(null);
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
        if (tameItems().test(stack)) {
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
    public boolean isFood(@NotNull ItemStack stack) {
        return foodItems().test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }

    @Override
    public boolean wantsToPickUp(@NotNull ServerLevel level, @NotNull ItemStack itemStack) {
        EquipmentSlot slot = getEquipmentSlotForItem(itemStack);
        return slot == EquipmentSlot.MAINHAND && this.getItemBySlot(slot).isEmpty() && foodItems().test(itemStack);
    }

    @Override
    protected void pickUpItem(@NotNull ServerLevel level, @NotNull ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (this.getMainHandItem().isEmpty() && foodItems().test(stack)) {
            this.onItemPickup(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(itemEntity, stack.getCount());
            itemEntity.discard();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
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
            this.setEating(true);
        } else if (this.getMainHandItem().isEmpty()) {
            this.setEating(false);
        }
        if (this.isEating()) {
            if (!this.level().isClientSide() && this.getEatCounter() > 6000) {
                if (!this.getMainHandItem().isEmpty()) {
                    if (!this.level().isClientSide()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.gameEvent(GameEvent.EAT);
                    }
                }
                this.setEating(false);
                return;
            }
            this.setEatCounter(this.getEatCounter() + 1);
        }
    }

    @Override
    public boolean canSleep() {
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return !this.level().isBrightOutside();
    }

    private boolean canRattle() {
        boolean rattlesnake = this.isRattlesnake();
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
        if(!players.isEmpty() && rattlesnake && !players.getFirst().isCreative()){
            this.setTarget(players.getFirst());
        } else {
            this.setTarget(null);
        }
        return !players.isEmpty() && rattlesnake;
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
                    if (this.mob.getNavigation().createPath(livingEntity, 0) != null) {
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
    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean sleeping = this.isSleeping() || this.isInSittingPose();
        boolean climbing = this.isClimbing();
        if (this.tongueTicks > 0) {
            this.tongueTicks--;
        } else if (!this.isSleeping() && this.random.nextInt(1000) < this.ambientSoundTime) {
            this.tongueTicks = 15;
        }
        this.attackAnimationState.animateWhen(this.attackAnimTimer.tick(this.swinging), this.tickCount);
        this.tongueAnimationState.animateWhen(this.tongueTicks > 0, this.tickCount);
        this.rattleAnimationState.animateWhen(this.canRattle() && !this.isSleeping(), this.tickCount);
        this.sleepAnimationState.animateWhen(sleeping, this.tickCount);
        this.climbAnimationState.animateWhen(!sleeping && climbing, this.tickCount);
        this.moveAnimationState.animateWhen(!sleeping && !climbing && this.walkAnimation.speed() > 0.04F, this.tickCount);
    }
    //endregion

    @Override
    protected void doPush(@NotNull Entity entity) {
    }
}
