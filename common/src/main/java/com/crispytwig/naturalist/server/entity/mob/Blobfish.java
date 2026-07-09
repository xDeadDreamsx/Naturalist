package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.server.entity.ai.goal.BlobfishStayDeepGoal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

@SuppressWarnings("unused")
public class Blobfish extends AbstractFish implements NaturalistGeoEntity, DataDrivenVariantAnimal {
    //region Data
    private static final int CONVERSION_DURATION = 100;

    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Blobfish.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_GRAY = SynchedEntityData.defineId(Blobfish.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CONVERTING = SynchedEntityData.defineId(Blobfish.class, EntityDataSerializers.BOOLEAN);

    private int conversionTime;

    public float xBodyRot;
    public float xBodyRotO;
    private Vec3 lastMoveDir = Vec3.ZERO;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.blobfish_gray.swim");
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.blobfish_pink.idle");

    public Blobfish(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.ATTACK_DAMAGE, 1.0D).add(Attributes.MOVEMENT_SPEED, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, this.defaultVariant().location().toString());
        builder.define(DATA_GRAY, true);
        builder.define(DATA_CONVERTING, false);
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/blobfish/pink.png");
    }

    @Override
    public String getVariantRawId() {
        return this.entityData.get(DATA_VARIANT);
    }

    @Override
    public void setVariantRawId(String id) {
        this.entityData.set(DATA_VARIANT, id);
    }

    public int getDeepY() {
        return this.level().getSeaLevel() - 33;
    }

    public boolean isGray() {
        return this.entityData.get(DATA_GRAY);
    }

    public void setGray(boolean gray) {
        this.entityData.set(DATA_GRAY, gray);
    }

    public boolean isConverting() {
        return this.entityData.get(DATA_CONVERTING);
    }

    private boolean wantsGray() {
        return this.isInWaterOrBubble() && this.getY() <= this.getDeepY();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveVariant(compound);
        compound.putBoolean("Gray", this.isGray());
        compound.putInt("ConversionTime", this.conversionTime);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.loadVariant(compound);
        this.setGray(compound.getBoolean("Gray"));
        this.conversionTime = compound.getInt("ConversionTime");
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NaturalistRegistry.BLOBFISH_BUCKET.get());
    }
    //endregion

    //region Spawning
    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        if (reason != MobSpawnType.BUCKET) {
            this.pickVariantForSpawn(level);
        }
        this.setGray(this.wantsGray());
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.5D, 2.0D));
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>(this, Axolotl.class, 6.0F, 1.5D, 2.0D));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.6D, true));
        this.goalSelector.addGoal(2, new BlobfishStayDeepGoal(this, 1.0D, 20));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                entity -> entity instanceof Crab || entity instanceof Snail));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.tickConversion();
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

    private void tickConversion() {
        boolean wantGray = this.wantsGray();
        if (wantGray == this.isGray()) {
            this.conversionTime = 0;
        } else if (++this.conversionTime >= CONVERSION_DURATION) {
            this.setGray(wantGray);
            this.conversionTime = 0;
            this.onTransform();
        }
        this.entityData.set(DATA_CONVERTING, this.conversionTime > 0);
    }

    private void onTransform() {
        if (this.level() instanceof ServerLevel serverLevel) {
            ItemParticleOption particle = new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(NaturalistRegistry.BLOBFISH.get()));
            for (int i = 0; i < 16; i++) {
                serverLevel.sendParticles(particle, this.getRandomX(1.0), this.getRandomY(), this.getRandomZ(1.0), 1, 0.0, 0.0, 0.0, 0.05);
            }
        }
        this.playSound(NaturalistSoundEvents.BLOBFISH_HURT.get(), this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return NaturalistSoundEvents.BLOBFISH_FLOP.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.BLOBFISH_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.BLOBFISH_DEATH.get();
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Blobfish> @NotNull PlayState predicate(final AnimationState<E> event) {
        AnimationController<E> controller = event.getController();
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6;
        boolean idle = this.onGround() && !moving;
        controller.setAnimationSpeed(idle ? 1.0 : 2.0);
        controller.setAnimation(idle ? IDLE : SWIM);
        return PlayState.CONTINUE;
    }

    private void soundListener(SoundKeyframeEvent<Blobfish> event) {
        Blobfish blobfish = event.getAnimatable();
        if (blobfish.level().isClientSide && "swim".equals(event.getKeyframeData().getSound())) {
            blobfish.level().playLocalSound(blobfish.getX(), blobfish.getY(), blobfish.getZ(), NaturalistSoundEvents.BLOBFISH_SWIM.get(), blobfish.getSoundSource(), 0.3F, 1.0F, false);
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate).setSoundKeyframeHandler(this::soundListener));
    }

    public float getXBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.xBodyRotO, this.xBodyRot);
    }
    //endregion
}
