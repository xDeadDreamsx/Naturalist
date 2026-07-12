package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.crispytwig.naturalist.server.entity.util.SmoothSpeedAnimationController;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

@SuppressWarnings("unused")
public class Bass extends AbstractSchoolingFish implements NaturalistGeoEntity, DataDrivenVariantAnimal {
    //region Data
    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Bass.class, EntityDataSerializers.STRING);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.bass.swim");
    protected static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.bass.flop");

    public Bass(EntityType<? extends AbstractSchoolingFish> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, this.defaultVariant().location().toString());
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/bass.png");
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
        return new ItemStack(NaturalistRegistry.BASS_BUCKET.get());
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
        super.registerGoals();
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 6.0F, 1.0D, 1.5D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Axolotl.class, 6.0F, 1.0D, 1.5D));
    }

    @Override
    public int getMaxSchoolSize() {
        return 5;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.SALMON_HURT;
    }

    @Override
    protected @NotNull SoundEvent getFlopSound() {
        return NaturalistSoundEvents.BASS_FLOP.get();
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getBoneResetTime() {
        return 2;
    }

    protected <E extends Bass> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (!this.isInWater()) {
            event.getController().setAnimation(FLOP);
            event.getController().setAnimationSpeed(1.0D);
        } else {
            event.getController().setAnimation(SWIM);
            event.getController().setAnimationSpeed(this.movementAnimationSpeed(event, 1.0D, SMALL_FISH_LIMB_SWING));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new SmoothSpeedAnimationController<>(this, "controller", 2, this::predicate));
    }
    //endregion
}
