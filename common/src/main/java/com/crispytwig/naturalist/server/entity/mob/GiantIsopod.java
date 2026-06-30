package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.base.HidingAnimal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
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
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

@SuppressWarnings("unused")
public class GiantIsopod extends Animal implements NaturalistGeoEntity, HidingAnimal, Bucketable {
    //region Data
    public static final int VARIANTS = 2;
    public static final String[] VARIANT_NAMES = {"brown", "blue"};

    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.GIANT_ISOPOD_FOOD);
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(GiantIsopod.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(GiantIsopod.class, EntityDataSerializers.BOOLEAN);

    private boolean wasHiding;
    private boolean hideAnimActive;
    private long hideCacheTick = -1L;
    private boolean hideCache;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.giant_isopod.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.giant_isopod.walk");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.giant_isopod.swim");
    protected static final RawAnimation HIDE = RawAnimation.begin().thenPlay("animation.sf_nba.giant_isopod.hide_start").thenLoop("animation.sf_nba.giant_isopod.hide_idle");
    protected static final RawAnimation HIDE_END = RawAnimation.begin().thenPlay("animation.sf_nba.giant_isopod.hide_end");

    public GiantIsopod(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.MOVEMENT_SPEED, 0.4D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(FROM_BUCKET, false);
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

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.entityData.set(FROM_BUCKET, fromBucket);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Variant", this.getVariant());
        compound.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setVariant(compound.getInt("Variant"));
        this.setFromBucket(compound.getBoolean("FromBucket"));
    }

    @Override
    public void saveToBucketTag(@NotNull ItemStack stack) {
        Bucketable.saveDefaultDataToBucketTag(this, stack);
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> tag.putInt("Variant", this.getVariant()));
        CompoundTag custom = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        custom.putInt("Variant", this.getVariant());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(custom));
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag tag) {
        Bucketable.loadDefaultDataFromBucketTag(this, tag);
        if (tag.contains("Variant")) {
            this.setVariant(tag.getInt("Variant"));
        }
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NaturalistRegistry.GIANT_ISOPOD_BUCKET.get());
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_FISH;
    }
    //endregion

    //region Spawning
    public static boolean checkGiantIsopodSpawnRules(EntityType<? extends GiantIsopod> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return pos.getY() < level.getLevel().getSeaLevel()
                && level.getFluidState(pos).is(FluidTags.WATER)
                && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        if (reason != MobSpawnType.BUCKET) {
            this.setVariant(this.random.nextInt(VARIANTS));
        }
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public GiantIsopod getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        GiantIsopod baby = NaturalistEntityTypes.GIANT_ISOPOD.get().create(level);
        if (baby != null && mob instanceof GiantIsopod other) {
            baby.setVariant(this.random.nextBoolean() ? this.getVariant() : other.getVariant());
        }
        return baby;
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Player.class, 5.0F, 1.5D, 2.5D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.1D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 1.0D, 30));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(false);
        return navigation;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.92F;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        return Bucketable.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    @Override
    public boolean canHide() {
        if (this.level().getGameTime() == this.hideCacheTick) {
            return this.hideCache;
        }
        this.hideCacheTick = this.level().getGameTime();
        this.hideCache = this.computeCanHide();
        return this.hideCache;
    }

    private boolean computeCanHide() {
        if (this.isBaby()) {
            return false;
        }
        List<Player> players = this.level().getNearbyPlayers(
                TargetingConditions.forNonCombat().range(3.0D).selector(EntitySelector.NO_CREATIVE_OR_SPECTATOR::test),
                this, this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D));
        return !players.isEmpty();
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(this.canHide() ? strength * 0.25D : strength, x, z);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return super.hurt(source, this.canHide() ? amount * 0.5F : amount);
    }

    @Override
    public void travel(@NotNull Vec3 vec3) {
        if (this.canHide()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
            vec3 = vec3.multiply(0.0D, 1.0D, 0.0D);
        }
        super.travel(vec3);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            boolean hiding = this.canHide();
            if (hiding) {
                this.jumping = false;
                this.xxa = 0.0F;
                this.zza = 0.0F;
                this.getNavigation().stop();
                if (!this.wasHiding) {
                    this.playSound(NaturalistSoundEvents.GIANT_ISOPOD_ROLL.get(), 0.6F, 1.0F);
                }
            }
            this.wasHiding = hiding;
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getBoneResetTime() {
        return 0.0;
    }

    protected <E extends GiantIsopod> @NotNull PlayState predicate(final AnimationState<E> event) {
        AnimationController<E> controller = event.getController();
        if (this.canHide()) {
            this.hideAnimActive = true;
            controller.setAnimation(HIDE);
            return PlayState.CONTINUE;
        }
        if (this.hideAnimActive) {
            controller.setAnimation(HIDE_END);
            if (!controller.hasAnimationFinished()) {
                return PlayState.CONTINUE;
            }
            this.hideAnimActive = false;
        }
        if (!this.onGround() && this.isInWater()) {
            controller.setAnimation(SWIM);
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            controller.setAnimation(WALK);
        } else {
            controller.setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    //endregion
}
