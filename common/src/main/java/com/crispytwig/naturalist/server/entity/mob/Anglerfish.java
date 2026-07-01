package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
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
public class Anglerfish extends AbstractFish implements NaturalistGeoEntity {
    //region Data
    public static final int VARIANTS = 2;
    public static final String[] VARIANT_NAMES = {"red", "glow"};

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Anglerfish.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_TARGET = SynchedEntityData.defineId(Anglerfish.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public float xBodyRot;
    public float xBodyRotO;
    private Vec3 lastMoveDir = Vec3.ZERO;

    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.anglerfish.swim");
    protected static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("animation.sf_nba.anglerfish.swim_fast");
    protected static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.anglerfish.flop");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.anglerfish.attack");

    public Anglerfish(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 8.0D).add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_HAS_TARGET, false);
    }

    public boolean hasSwimTarget() {
        return this.entityData.get(DATA_HAS_TARGET);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    public String getVariantName() {
        return VARIANT_NAMES[Math.floorMod(this.getVariant(), VARIANTS)];
    }

    public boolean isGlowing() {
        return this.getVariant() == 1;
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
            this.setVariant(isGlowSquidWater(level, this.blockPosition()) ? 1 : 0);
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5D, true));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0D, 20));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, WaterAnimal.class, 10, true, false,
                entity -> entity.getType().is(NaturalistTags.EntityTypes.ANGLERFISH_HOSTILES)));
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
    public boolean doHurtTarget(@NotNull Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && !this.level().isClientSide) {
            this.triggerAnim("attackController", "attack");
        }
        return result;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_HAS_TARGET, this.getTarget() != null);
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
        } else if (this.hasSwimTarget()) {
            controller.setAnimation(SWIM_FAST);
        } else {
            controller.setAnimation(SWIM);
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(SoundKeyframeEvent<Anglerfish> event) {
        Anglerfish anglerfish = event.getAnimatable();
        if (anglerfish.level().isClientSide && "swim".equals(event.getKeyframeData().getSound())) {
            anglerfish.level().playLocalSound(anglerfish.getX(), anglerfish.getY(), anglerfish.getZ(), NaturalistSoundEvents.ANGLERFISH_SWIM.get(), anglerfish.getSoundSource(), 0.3F, 1.0F, false);
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate).setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "attackController", 0, state -> PlayState.STOP)
                .triggerableAnim("attack", ATTACK));
    }
    //endregion
}
