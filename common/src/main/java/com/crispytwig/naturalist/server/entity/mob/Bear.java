package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.server.entity.base.DyeableAnimal;
import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.PetTargeting;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.*;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Containers;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class Bear extends TamableAnimal implements NeutralMob, NaturalistGeoEntity, SleepingAnimal, DyeableAnimal, FollowingPet, HuntingAnimal {
    //region Data
    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.BEAR_TEMPT_ITEMS);
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SNIFFING = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DYE = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> REMAINING_ANGER_TIME = SynchedEntityData.defineId(Bear.class, EntityDataSerializers.INT);

    private boolean followingOwner = true;
    private int huntingCooldown;
    @Nullable
    private UUID persistentAngerTarget;
    private boolean wasSitting;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.bear.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.bear.walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.sf_nba.bear.run");
    protected static final RawAnimation SIT = RawAnimation.begin().thenPlay("animation.sf_nba.bear.sit");
    protected static final RawAnimation UNSIT = RawAnimation.begin().thenPlay("animation.sf_nba.bear.unsit");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.sf_nba.bear.sleep");
    protected static final RawAnimation SNIFF = RawAnimation.begin().thenLoop("animation.sf_nba.bear.sniff");
    protected static final RawAnimation EAT = RawAnimation.begin().thenLoop("animation.sf_nba.bear.eat");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.bear.attack");

    public Bear(@NotNull EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
        builder.define(SNIFFING, false);
        builder.define(SITTING, false);
        builder.define(SHEARED, false);
        builder.define(EAT_COUNTER, 0);
        builder.define(REMAINING_ANGER_TIME, 0);
        builder.define(DATA_DYE, -1);
    }

    @Nullable
    @Override
    public DyeColor getDyeColor() {
        int id = this.entityData.get(DATA_DYE);
        return id < 0 ? null : DyeColor.byId(id);
    }

    @Override
    public void setDyeColor(@Nullable DyeColor color) {
        this.entityData.set(DATA_DYE, color == null ? -1 : color.getId());
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    @Override
    public boolean canSleep() {
        long dayTime = this.level().getDayTime();
        return (dayTime < 12000 || dayTime > 18000) && dayTime < 23000 && dayTime > 6000 && !this.isAngry() && !this.level().isWaterAt(this.blockPosition());
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean isSniffing() {
        return this.entityData.get(SNIFFING);
    }

    public void setSniffing(boolean sniffing) {
        this.entityData.set(SNIFFING, sniffing);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    public boolean isEating() {
        return this.entityData.get(EAT_COUNTER) > 0;
    }

    public void eat(boolean eat) {
        this.entityData.set(EAT_COUNTER, eat ? 1 : 0);
    }

    public int getEatCounter() {
        return this.entityData.get(EAT_COUNTER);
    }

    private void setEatCounter(int amount) {
        this.entityData.set(EAT_COUNTER, amount);
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
    public int getHuntingCooldown() {
        return this.huntingCooldown;
    }

    @Override
    public void setHuntingCooldown(int ticks) {
        this.huntingCooldown = ticks;
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
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.addPersistentAngerSaveData(compound);
        compound.putBoolean("Sheared", this.isSheared());
        DyeableAnimal.saveDye(this, compound);
        FollowingPet.save(this, compound);
        this.addHuntingCooldownSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readPersistentAngerSaveData(this.level(), compound);
        if (compound.contains("Sheared")) {
            this.setSheared(compound.getBoolean("Sheared"));
        }
        DyeableAnimal.loadDye(this, compound);
        FollowingPet.load(this, compound);
        this.readHuntingCooldownSaveData(compound);
    }
    //endregion

    //region Spawning
    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        if (spawnData == null) {
            spawnData = new AgeableMobGroupData(1.0F);
        }

        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.setCanPickUpLoot(true);
        return data;
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.BEAR.get().create(serverLevel);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new BearFloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new BearMeleeAttackGoal(this, 1.25D, true));
        this.goalSelector.addGoal(5, new PetFollowOwnerGoal(this, 1.25D, 10.0F, 6.0F));
        this.goalSelector.addGoal(3, new BearSleepGoal(this));
        this.goalSelector.addGoal(4, new BearTemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(4, new BabyPanicGoal(this, 1.25D));
        this.goalSelector.addGoal(5, new DistancedFollowParentGoal(this, 1.25D, 48.0D, 8.0D, 12.0D));
        this.goalSelector.addGoal(5, new SearchForItemsGoal(this, 1.2F, FOOD_ITEMS, 8, 2));
        this.goalSelector.addGoal(6, new BearHarvestFoodGoal(this, 1.2F, 12, 3));
        this.goalSelector.addGoal(6, new BabyBearSniffFlowersGoal(this, 1.2F, 12, 3));
        this.goalSelector.addGoal(7, new BearPickupFoodAndSitGoal(this));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new BabyHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(2, new BearAttackPlayerNearBabiesGoal(this, Player.class, 20, false, true, null));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, PathfinderMob.class, 10, true, false, (entity) -> entity.getType().is(NaturalistTags.EntityTypes.BEAR_HOSTILES) && !this.isSleeping() && !this.isBaby() && this.hasHuntingCooldown()));
        this.targetSelector.addGoal(5, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public boolean wantsToAttack(@NotNull LivingEntity target, @NotNull LivingEntity owner) {
        return PetTargeting.wantsToAttack(target, owner);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98F;
    }

    @Override
    public void knockback(double strength, double x, double z) {
        if (this.isBaby()) {
            double knockbackResistance = this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            super.knockback(strength / Math.max(1.0 - knockbackResistance, 0.01), x, z);
        } else {
            super.knockback(strength, x, z);
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (!this.getMainHandItem().isEmpty() && !this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, this.getMainHandItem());
            itemEntity.setPickUpDelay(80);
            itemEntity.setThrower(this);
            this.playSound(NaturalistSoundEvents.BEAR_SPIT.get(), 1.0F, 1.0F);
            this.level().addFreshEntity(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        if (this.isTame() && this.getOwnerUUID() != null && target instanceof OwnableEntity ownable
                && this.getOwnerUUID().equals(ownable.getOwnerUUID())) {
            return false;
        }
        return super.canAttack(target);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.equals(this.damageSources().sweetBerryBush()) || super.isInvulnerableTo(source);
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
        ItemStack itemStack = player.getItemInHand(hand);
        InteractionResult whistle = FollowingPet.tryWhistle(this, player, hand);
        if (whistle != null) {
            return whistle;
        }
        Optional<InteractionResult> dyeClear = DyeableAnimal.tryClearDye(this, player, hand);
        if (dyeClear.isPresent()) {
            return dyeClear.get();
        }
        if (itemStack.is(Items.SHEARS) && this.isShearable(player, itemStack, this.level(), this.blockPosition())) {
            if (!this.isSleeping()) {
                this.setLastHurtByMob(player);
            }
            List<ItemStack> drops = this.onSheared(player, itemStack, this.level(), this.blockPosition());
            if (!this.level().isClientSide) {
                for (ItemStack drop : drops) {
                    this.spawnShearedDrop(this.level(), this.blockPosition(), drop);
                }
                itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
            this.gameEvent(GameEvent.SHEAR, player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        Optional<InteractionResult> dye = DyeableAnimal.tryDye(this, player, hand);
        if (dye.isPresent()) {
            return dye.get();
        }
        if (!this.isTame() && this.isBaby() && this.isFood(itemStack)) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isTame() && this.isOwnedBy(player)) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
                this.heal(4.0F);
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            if (itemStack.isEmpty() && player.isSecondaryUseActive()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.navigation.stop();
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canTakeItem(@NotNull ItemStack itemStack) {
        EquipmentSlot slot = this.getEquipmentSlotForItem(itemStack);
        if (!this.getItemBySlot(slot).isEmpty() || this.isBaby()) {
            return false;
        } else {
            return slot == EquipmentSlot.MAINHAND && super.canTakeItem(itemStack);
        }
    }

    @Override
    protected void pickUpItem(@NotNull ItemEntity itemEntity) {
        ItemStack stack = itemEntity.getItem();
        if (this.getMainHandItem().isEmpty() && FOOD_ITEMS.test(stack) && !this.isBaby()) {
            this.onItemPickup(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(itemEntity, stack.getCount());
            itemEntity.discard();
        }
    }

    public boolean isShearable(@Nullable Player player, @NotNull ItemStack item, @NotNull Level level, @NotNull BlockPos pos) {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, @NotNull Level level, @NotNull BlockPos pos) {
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, player != null ? SoundSource.PLAYERS : SoundSource.BLOCKS, 1.0f, 1.0f);
        this.setSheared(true);
        int amount = 1 + this.random.nextInt(2);
        List<ItemStack> drops = new ArrayList<>();
        for (int j = 0; j < amount; ++j) {
            drops.add(new ItemStack(NaturalistRegistry.FUR.get()));
        }
        return drops;
    }

    private void spawnShearedDrop(Level level, BlockPos pos, ItemStack drop) {
        ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY() + 1.0, pos.getZ(), drop);
        itemEntity.setDeltaMovement(
                (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                this.random.nextFloat() * 0.05F,
                (this.random.nextFloat() - this.random.nextFloat()) * 0.1F);
        level.addFreshEntity(itemEntity);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
        this.handleEating();
        if (!this.getMainHandItem().isEmpty()) {
            if (this.isAngry()) {
                this.stopBeingAngry();
            }
            this.setSniffing(false);
        }
        this.level().getProfiler().push("looting");
        if (!this.level().isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D))) {
                if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && this.wantsToPickUp(itementity.getItem())) {
                    this.pickUpItem(itementity);
                }
            }
        }
        this.level().getProfiler().pop();
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.tickHuntingCooldown();
        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 1.25D);
        } else {
            this.setSprinting(false);
        }
    }

    private void handleEating() {
        if (!this.level().isClientSide) {
            if (!this.isEating() && this.isSitting() && !this.isSleeping() && !this.getMainHandItem().isEmpty() && this.random.nextInt(40) == 1) {
                this.eat(true);
            } else if (this.getMainHandItem().isEmpty() || !this.isSitting()) {
                this.eat(false);
            }
            if (this.isEating()) {
                if (this.getEatCounter() > 40) {
                    if (this.isFood(this.getItemBySlot(EquipmentSlot.MAINHAND))) {
                        boolean wasSalmon = this.getItemBySlot(EquipmentSlot.MAINHAND).is(Items.SALMON);
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.gameEvent(GameEvent.EAT);
                        this.setSheared(false);
                        this.setSitting(false);
                        if (wasSalmon) {
                            double facing = Math.toRadians(this.yBodyRot);
                            double spawnX = this.getX() - Math.sin(facing) * 1.5D;
                            double spawnZ = this.getZ() + Math.cos(facing) * 1.5D;
                            ItemEntity boneMeal = new ItemEntity(this.level(), spawnX, this.getY() + 0.5D, spawnZ, new ItemStack(Items.BONE_MEAL));
                            boneMeal.setDefaultPickUpDelay();
                            this.level().addFreshEntity(boneMeal);
                        }
                    }
                    this.eat(false);
                    return;
                }
                this.setEatCounter(this.getEatCounter() + 1);
                if (this.getEatCounter() % 5 == 0 || this.getEatCounter() == 1) {
                    this.playSound(NaturalistSoundEvents.BEAR_EAT.get(), 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                }
            }
        } else if (this.isEating()) {
            this.addEatingParticles();
        }
    }

    private void addEatingParticles() {
        if (this.getEatCounter() % 5 == 0 || this.getEatCounter() == 1) {
            for(int i = 0; i < 6; ++i) {
                Vec3 speedVec = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.1D, Math.random() * 0.1D + 0.1D, ((double)this.random.nextFloat() - 0.5D) * 0.1D);
                speedVec  = speedVec .xRot(-this.getXRot() * ((float)Math.PI / 180F));
                speedVec  = speedVec .yRot(-this.getYRot() * ((float)Math.PI / 180F));
                double y = (double)(-this.random.nextFloat()) * 0.6D - 0.3D;
                Vec3 posVec = new Vec3(((double)this.random.nextFloat() - 0.5D) * 0.8D, y, 1.0D + ((double)this.random.nextFloat() - 0.5D) * 0.4D);
                posVec = posVec.yRot(-this.yBodyRot * ((float)Math.PI / 180F));
                posVec = posVec.add(this.getX(), this.getEyeY() - 0.2D, this.getZ() - 0.1D);
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlot.MAINHAND)), posVec.x, posVec.y, posVec.z, speedVec .x, speedVec .y + 0.05D, speedVec .z);
            }
        }
    }

    void tryToSit() {
        if (this.isTouchingWater()) {
            this.setZza(0.0F);
            this.getNavigation().stop();
            this.setSitting(true);
        }
    }

    boolean isTouchingWater() {
        return !this.level().isWaterAt(this.blockPosition());
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSleeping() ? NaturalistSoundEvents.BEAR_SLEEP.get() : this.isBaby() ? NaturalistSoundEvents.BEAR_AMBIENT_BABY.get() : NaturalistSoundEvents.BEAR_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return this.isBaby() ? NaturalistSoundEvents.BEAR_HURT_BABY.get() : NaturalistSoundEvents.BEAR_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? NaturalistSoundEvents.BEAR_DEATH_BABY.get() : NaturalistSoundEvents.BEAR_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState block) {
        this.playSound(SoundEvents.POLAR_BEAR_STEP, 0.15F, 1.0F);
    }

    @Override
    public float getVoicePitch() {
        return this.isSleeping() ? super.getVoicePitch() * 0.3F : super.getVoicePitch();
    }

    static class BearAttackPlayerNearBabiesGoal extends NearestAttackableTargetGoal<Player> {
        private final Bear bear;

        public BearAttackPlayerNearBabiesGoal(Bear mob, Class<Player> targetType, int randomInterval, boolean mustSee, boolean mustReach, @Nullable Predicate<LivingEntity> targetPredicate) {
            super(mob, targetType, randomInterval, mustSee, mustReach, targetPredicate);
            this.bear = mob;
        }

        @Override
        public boolean canUse() {
            if (!bear.isBaby() && !bear.isSleeping()) {
                if (super.canUse()) {
                    for (Bear bear : bear.level().getEntitiesOfClass(Bear.class, bear.getBoundingBox().inflate(8.0D, 4.0D, 8.0D))) {
                        if (bear.isBaby()) {
                            return true;
                        }
                    }
                }

            }
            return false;
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.5D;
        }
    }

    class BearSleepGoal extends SleepGoal<Bear> {
        public BearSleepGoal(Bear animal) {
            super(animal);
        }

        @Override
        public void start() {
            Bear.this.setSniffing(false);
            super.start();
        }
    }

    static class BearHarvestFoodGoal extends MoveToBlockGoal {
        protected int ticksWaited;
        private final @NotNull Bear bear;

        public BearHarvestFoodGoal(@NotNull Bear mob, double speedModifier, int searchRange, int verticalSearchRange) {
            super(mob, speedModifier, searchRange, verticalSearchRange);
            this.bear = mob;
        }

        @Override
        public double acceptedDistance() {
            return 3.0D;
        }

        public boolean shouldRecalculatePath() {
            return this.tryTicks % 100 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader level, @NotNull BlockPos pos) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof BeehiveBlock) {
                return state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5;
            } else if (state.is(Blocks.SWEET_BERRY_BUSH)) {
                return state.getValue(SweetBerryBushBlock.AGE) >= 2;
            } else if (state.is(Blocks.CAMPFIRE) && level.getBlockEntity(pos) instanceof CampfireBlockEntity campfire) {
                return campfireIsTempting(campfire);
            }
            return false;
        }

        @Override
        public void tick() {
            if (this.isReachedTarget()) {
                if (this.ticksWaited >= 40) {
                    this.onReachedTarget();
                } else {
                    ++this.ticksWaited;
                }
            } else if (!this.isReachedTarget() && bear.getRandom().nextFloat() < 0.05F) {
                bear.playSound(NaturalistSoundEvents.BEAR_SNIFF.get(), 1.0F, 1.0F);
                bear.setSniffing(true);
            }
            bear.getLookControl().setLookAt(blockPos.getX() + 0.5D, blockPos.getY(), blockPos.getZ() + 0.5D, 10.0F, bear.getMaxHeadXRot());
            super.tick();
        }

        protected void onReachedTarget() {
            if (bear.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                BlockState state = bear.level().getBlockState(blockPos);
                bear.setSniffing(false);
                if (state.getBlock() instanceof BeehiveBlock && state.getValue(BeehiveBlock.HONEY_LEVEL) >= 5) {
                    this.harvestHoney(state);
                } else if (state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) >= 2) {
                    this.pickSweetBerries(state);
                } else if (state.is(Blocks.CAMPFIRE) && bear.level().getBlockEntity(blockPos) instanceof CampfireBlockEntity campfire && campfireIsTempting(campfire)) {
                    this.stealCampfireFood(state, campfire);
                }
            }
        }

        private void stealCampfireFood(BlockState state, @NotNull CampfireBlockEntity campfire) {
            for (int i = 0; i < campfire.getItems().size(); i++) {
                if (FOOD_ITEMS.test(campfire.getItems().get(i))) {
                    Containers.dropItemStack(bear.level(), blockPos.getX(), blockPos.getY(), blockPos.getZ(), campfire.getItems().get(i));
                    campfire.getItems().set(i, ItemStack.EMPTY);
                    bear.level().sendBlockUpdated(blockPos, state, state, 3);
                    campfire.setChanged();
                    break;
                }
            }
        }

        private boolean campfireIsTempting(@NotNull CampfireBlockEntity campfire) {
            for (int i = 0; i < campfire.getItems().size(); i++) {
                if (FOOD_ITEMS.test(campfire.getItems().get(i))) {
                    return true;
                }
            }
            return false;
        }

        private void harvestHoney(BlockState state) {
            state.setValue(BeehiveBlock.HONEY_LEVEL, 0);
            BeehiveBlock.dropHoneycomb(bear.level(), blockPos);
            bear.playSound(SoundEvents.BEEHIVE_SHEAR, 1.0F, 1.0F);
            bear.level().setBlock(blockPos, state.setValue(BeehiveBlock.HONEY_LEVEL, 0), 2);
            bear.swing(InteractionHand.MAIN_HAND);
        }

        private void pickSweetBerries(@NotNull BlockState state) {
            int age = state.getValue(SweetBerryBushBlock.AGE);
            state.setValue(SweetBerryBushBlock.AGE, 1);
            int berryAmount = 1 + bear.level().random.nextInt(2) + (age == 3 ? 1 : 0);
            Block.popResource(bear.level(), this.blockPos, new ItemStack(Items.SWEET_BERRIES, berryAmount));
            bear.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
            bear.level().setBlock(this.blockPos, state.setValue(SweetBerryBushBlock.AGE, 1), 2);
            bear.swing(InteractionHand.MAIN_HAND);
        }

        @Override
        public boolean canUse() {
            return !bear.isBaby() && bear.getMainHandItem().isEmpty() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return bear.getMainHandItem().isEmpty() && super.canContinueToUse();
        }

        @Override
        public void start() {
            super.start();
            this.ticksWaited = 0;
        }

        @Override
        protected @NotNull BlockPos getMoveToTarget() {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(blockPos);
            while (bear.level().getBlockState(mutable.below()).isAir()) {
                mutable.move(Direction.DOWN);
            }
            return mutable;
        }
    }

    static class BabyBearSniffFlowersGoal extends BabySniffFlowersGoal {
        private final Bear bear;

        public BabyBearSniffFlowersGoal(@NotNull Bear bear, double speedModifier, int searchRange, int verticalSearchRange) {
            super(bear, speedModifier, searchRange, verticalSearchRange, NaturalistSoundEvents.BEAR_SNIFF.get());
            this.bear = bear;
        }

        @Override
        public boolean canUse() {
            return this.bear.getMainHandItem().isEmpty() && super.canUse();
        }

        @Override
        public void tick() {
            super.tick();
            this.bear.setSniffing(this.isReachedTarget());
        }

        @Override
        public void stop() {
            super.stop();
            this.bear.setSniffing(false);
        }
    }

    static class BearFloatGoal extends FloatGoal {
        private final Bear bear;

        public BearFloatGoal(Bear mob) {
            super(mob);
            this.bear = mob;
        }

        @Override
        public boolean canUse() {
            if (!bear.isBaby()) {
                return (bear.level().isWaterAt(bear.blockPosition().below()) || bear.level().isWaterAt(bear.blockPosition().above())) && super.canUse();
            } else {
                return super.canUse();
            }
        }
    }

    static class BearTemptGoal extends TemptGoal {
        private final Bear bear;

        public BearTemptGoal(Bear mob, double speedModifier, Ingredient items, boolean canScare) {
            super(mob, speedModifier, items, canScare);
            this.bear = mob;
        }

        @Override
        public boolean canUse() {
            return bear.getMainHandItem().isEmpty() && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            bear.setSniffing(true);
        }

        @Override
        public void tick() {
            super.tick();
            if (bear.getRandom().nextFloat() < 0.05F) {
                bear.playSound(NaturalistSoundEvents.BEAR_SNIFF.get(), 1.0F, 1.0F);
            }
        }

        @Override
        public void stop() {
            super.stop();
            bear.setSniffing(false);
        }
    }

    static class BearPickupFoodAndSitGoal extends Goal {
        private int cooldown;
        private final Bear bear;

        public BearPickupFoodAndSitGoal(Bear bear) {
            this.setFlags(EnumSet.of(Flag.MOVE));
            this.bear = bear;
        }

        @Override
        public boolean canUse() {
            if (this.cooldown <= bear.tickCount && !bear.isBaby() && bear.isTouchingWater() && !bear.isSleeping() && !bear.isSitting()) {
                return !bear.getMainHandItem().isEmpty();
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return bear.isTouchingWater();
        }

        @Override
        public void tick() {
            if (!bear.isSitting() && !bear.getMainHandItem().isEmpty()) {
                bear.tryToSit();
            }
        }

        @Override
        public void start() {
            if (!bear.getMainHandItem().isEmpty()) {
                bear.tryToSit();
            }

            this.cooldown = 0;
        }

        @Override
        public void stop() {
            ItemStack stack = bear.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!stack.isEmpty()) {
                bear.spawnAtLocation(stack);
                bear.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                int cooldownSeconds = bear.random.nextInt(150) + 10;
                this.cooldown = bear.tickCount + cooldownSeconds * 20;
            }
            bear.setSitting(false);
        }
    }

    static class BearMeleeAttackGoal extends MeleeAttackGoal {
        public BearMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return mob.getMainHandItem().isEmpty() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getMainHandItem().isEmpty() && super.canContinueToUse();
        }

    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Bear> PlayState predicate(final AnimationState<E> event) {
        boolean sitting = this.isSitting() || this.isInSittingPose();

        if (this.isSleeping()) {
            event.getController().setAnimation(SLEEP);
            this.wasSitting = sitting;
            return PlayState.CONTINUE;
        } else if (sitting) {
            event.getController().setAnimation(SIT);
            this.wasSitting = true;
            return PlayState.CONTINUE;
        } else if (this.wasSitting) {
            this.wasSitting = false;
            event.getController().setAnimation(UNSIT);
            return PlayState.CONTINUE;
        } else if (!event.getController().getAnimationState().equals(AnimationController.State.STOPPED)
                && event.getController().getCurrentAnimation() != null
                && event.getController().getCurrentAnimation().animation().name().equals("animation.sf_nba.bear.unsit")) {
            return PlayState.CONTINUE;
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            if (this.isSprinting()) {
                event.getController().setAnimation(RUN);
                event.getController().setAnimationSpeed(2.0D);
            } else {
                event.getController().setAnimation(WALK);
                event.getController().setAnimationSpeed(1.4D);
            }
            return PlayState.CONTINUE;
        } else {
            event.getController().setAnimation(IDLE);
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    protected <E extends Bear> PlayState sniffPredicate(final @NotNull AnimationState<E> event) {
        if (this.isSniffing()) {
            event.getController().setAnimation(SNIFF);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    protected <E extends Bear> PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimationSpeed(1.3F);
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    protected <E extends Bear> @NotNull PlayState eatPredicate(final AnimationState<E> event) {
        if (this.isEating()) {
            event.getController().setAnimation(EAT);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
        controllers.add(new AnimationController<>(this, "sniffController", 2, this::sniffPredicate));
        controllers.add(new AnimationController<>(this, "swingController", 2, this::attackPredicate));
        controllers.add(new AnimationController<>(this, "eatController", 5, this::eatPredicate));
    }
    //endregion
}
