package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistMobVariants;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.entity.variant.MobVariant;
import com.crispytwig.naturalist.server.entity.variant.MobVariantUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
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
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("unused")
public class Anglerfish extends AbstractFish implements NaturalistGeoEntity, HuntingAnimal, DataDrivenVariantAnimal {
    //region Data
    public static final String[] VARIANT_NAMES = {"red", "glow"};

    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Anglerfish.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TARGET = SynchedEntityData.defineId(Anglerfish.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int huntingCooldown;

    public float xBodyRot;
    public float xBodyRotO;
    private Vec3 lastMoveDir = Vec3.ZERO;

    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.anglerfish.swim");
    protected static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("animation.sf_nba.anglerfish.swim_fast");
    protected static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.anglerfish.flop");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.anglerfish.attack");

    public Anglerfish(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.ATTACK_DAMAGE, 2.0D).add(Attributes.MOVEMENT_SPEED, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, NaturalistMobVariants.ANGLERFISH_RED.location().toString());
        builder.define(DATA_HAS_TARGET, false);
    }

    public boolean hasSwimTarget() {
        return this.entityData.get(DATA_HAS_TARGET);
    }

    @Override
    public ResourceKey<MobVariant> defaultVariant() {
        return NaturalistMobVariants.ANGLERFISH_RED;
    }

    @Override
    public String[] legacyVariantNames() {
        return VARIANT_NAMES;
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/anglerfish/red.png");
    }

    @Override
    public String getVariantRawId() {
        return this.entityData.get(DATA_VARIANT);
    }

    @Override
    public void setVariantRawId(String id) {
        this.entityData.set(DATA_VARIANT, id);
    }

    public boolean isGlowing() {
        return NaturalistMobVariants.ANGLERFISH_GLOW.location().equals(this.getVariantId());
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveVariant(compound);
        this.addHuntingCooldownSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.loadVariant(compound);
        this.readHuntingCooldownSaveData(compound);
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
    public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity killed) {
        boolean result = super.killedEntity(level, killed);
        if (result) {
            this.startHuntingCooldown();
        }
        return result;
    }

    @Override
    public void saveToBucketTag(@NotNull ItemStack stack) {
        super.saveToBucketTag(stack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, this::saveVariant);
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        this.saveVariant(custom);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag tag) {
        super.loadFromBucketTag(tag);
        this.loadVariant(tag);
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NaturalistRegistry.ANGLERFISH_BUCKET.get());
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return NaturalistSoundEvents.CATFISH_FLOP.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.ANGLERFISH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.ANGLERFISH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.ANGLERFISH_DEATH.get();
    }
    //endregion

    //region Spawning
    public static boolean checkAnglerfishSpawnRules(EntityType<? extends WaterAnimal> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return WaterAnimal.checkSurfaceWaterAnimalSpawnRules(type, level, reason, pos, random) || isGlowSquidWater(level, pos);
    }

    private static boolean isGlowSquidWater(ServerLevelAccessor level, BlockPos pos) {
        return pos.getY() <= level.getLevel().getSeaLevel() - 33 && level.getRawBrightness(pos, 0) == 0 && level.getBlockState(pos).is(Blocks.WATER);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        if (reason != MobSpawnType.BUCKET) {
            if (isGlowSquidWater(level, this.blockPosition())) {
                MobVariantUtil.byKey(level.registryAccess(), NaturalistMobVariants.ANGLERFISH_VARIANT, NaturalistMobVariants.ANGLERFISH_GLOW)
                        .ifPresent(this::setVariant);
            } else {
                this.pickVariantForSpawn(level);
            }
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 2.2D, true) {
            @Override
            protected void checkAndPerformAttack(@NotNull LivingEntity target) {
                double reach = this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + target.getBbWidth();
                if (this.isTimeToAttack() && this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ()) <= reach) {
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(target);
                }
            }
        });
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0D, 20));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, WaterAnimal.class, 10, true, false,
                entity -> this.hasHuntingCooldown() && entity.getType().is(NaturalistTags.EntityTypes.ANGLERFISH_HOSTILES)));
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
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_HAS_TARGET, this.getTarget() != null);
            this.tickHuntingCooldown();
        }

        this.xBodyRotO = this.xBodyRot;
        float target = 0.0F;
        if (this.isInWater()) {
            Vec3 movement = this.getDeltaMovement();
            if (movement.lengthSqr() > 1.0E-6) {
                this.lastMoveDir = movement;
            }
            target = -((float) Mth.atan2(this.lastMoveDir.y, this.lastMoveDir.horizontalDistance()) * (180.0F / (float) Math.PI));
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
        return 5;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Anglerfish> @NotNull PlayState predicate(final AnimationState<E> event) {
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

    private void soundListener(SoundKeyframeEvent<Anglerfish> event) {
        Anglerfish anglerfish = event.getAnimatable();
        if (anglerfish.level().isClientSide && "swim".equals(event.getKeyframeData().getSound())) {
            anglerfish.level().playLocalSound(anglerfish.getX(), anglerfish.getY(), anglerfish.getZ(), NaturalistSoundEvents.ANGLERFISH_SWIM.get(), anglerfish.getSoundSource(), 0.3F, 1.0F, false);
        }
    }

    private <E extends Anglerfish> PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new SmoothSpeedAnimationController<>(this, "controller", 5, this::predicate).setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));
    }
    //endregion
}
