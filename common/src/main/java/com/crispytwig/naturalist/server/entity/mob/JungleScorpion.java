package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.server.entity.base.VariantAnimal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.RawAnimation;

public class JungleScorpion extends Scorpion implements VariantAnimal {
    //region Data
    public static final String[] VARIANT_NAMES = {"black", "green"};

    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(JungleScorpion.class, EntityDataSerializers.INT);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.jungle_scorpion.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.jungle_scorpion.walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.sf_nba.jungle_scorpion.run");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.jungle_scorpion.attack");

    public JungleScorpion(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level, IDLE, WALK, RUN, ATTACK);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
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
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt(VARIANT_TAG, this.getVariant());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt(VARIANT_TAG));
    }
    //endregion

    //region Spawning
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(this.random.nextInt(VARIANT_NAMES.length));
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
    //endregion

    //region Behavior
    @Override
    protected float getSoundVolume() {
        return 0.9F;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.75F;
    }
    //endregion
}
