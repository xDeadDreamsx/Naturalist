package com.crispytwig.naturalist.server.entity.misc;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class DirtTrail extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> SMALL = SynchedEntityData.defineId(DirtTrail.class, EntityDataSerializers.BOOLEAN);
    private static final int LIFETIME = 50;

    protected static final RawAnimation SPAWN = RawAnimation.begin().thenPlay("animation.sf_nba.dirt_trail.spawn");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public DirtTrail(EntityType<? extends DirtTrail> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(SMALL, false);
    }

    public boolean isSmall() {
        return this.entityData.get(SMALL);
    }

    public void setSmall(boolean small) {
        this.entityData.set(SMALL, small);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel serverLevel && this.tickCount >= LIFETIME) {
            double scale = this.isSmall() ? 0.6D : 1.0D;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()),
                    this.getX(), this.getY() + 0.1D * scale, this.getZ(),
                    this.isSmall() ? 12 : 20, 0.25D * scale, 0.05D, 0.25D * scale, 0.05D);
            this.discard();
        }
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            event.getController().setAnimation(SPAWN);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
