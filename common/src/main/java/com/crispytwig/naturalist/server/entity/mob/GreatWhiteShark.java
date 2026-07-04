package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.base.HuntingAnimal;
import com.crispytwig.naturalist.server.entity.base.MultipartMob;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.util.BeachedMob;
import com.crispytwig.naturalist.server.entity.util.BodyChain;
import com.crispytwig.naturalist.server.entity.util.MobPart;
import com.crispytwig.naturalist.server.entity.util.MultipartLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
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

import java.util.EnumSet;

public class GreatWhiteShark extends Animal implements NaturalistGeoEntity, MultipartMob, HuntingAnimal {
    //region Data
    private static final double[] PART_Z = {2.0D, -1.5D, -2.9D};
    private static final float[][] PART_SIZES = {{1.2F, 1.2F}, {1.0F, 1.0F}, {0.8F, 1.2F}};

    private static final float MAX_TILT = 40.0F;
    private static final float ATTACK_CONE_COS = Mth.cos(60.0F * Mth.DEG_TO_RAD);
    private static final ResourceLocation AGGRO_SPEED_BOOST_ID = ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "shark_aggro_speed_boost");
    private static final AttributeModifier AGGRO_SPEED_BOOST = new AttributeModifier(AGGRO_SPEED_BOOST_ID, 0.4D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.GREAT_WHITE_SHARK_FOOD_ITEMS);

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int flopCooldown;
    private int huntingCooldown;

    private final MobPart[] parts;
    private boolean partsRegistered;

    public float xBodyRot;
    public float xBodyRotO;
    private final BodyChain chain = new BodyChain(0.2F,
            new float[]{0.35F, 0.16F, 0.12F},
            new float[]{0.24F, 0.12F, 0.1F},
            4.0F, 25.0F, 0.1F);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.shark.idle");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.shark.swim");
    protected static final RawAnimation SWIM_FAST = RawAnimation.begin().thenLoop("animation.sf_nba.shark.swim_fast");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.shark.attack");
    protected static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.shark.flop");

    public GreatWhiteShark(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.1F, 0.5F, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.parts = new MobPart[PART_Z.length];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = new MobPart(this, PART_SIZES[i][0], PART_SIZES[i][1]);
        }
    }

    @Override
    public MobPart[] getParts() {
        return this.parts;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D);
    }

    @Override
    public void baseTick() {
        int air = this.getAirSupply();
        super.baseTick();
        if (!this.isAlive() || this.isInWaterOrBubble()) {
            this.setAirSupply(this.getMaxAirSupply());
            return;
        }
        this.setAirSupply(air - 1);
        if (this.getAirSupply() == -20) {
            this.setAirSupply(0);
            this.hurt(this.damageSources().drown(), 2.0F);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.9F;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.addHuntingCooldownSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
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
    //endregion

    //region Spawning
    @SuppressWarnings("unused")
    public static boolean checkGreatWhiteSharkSpawnRules(EntityType<GreatWhiteShark> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER) && level.getFluidState(pos.above()).is(FluidTags.WATER);
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        return null;
    }
    //endregion

    //region Behavior
    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SharkAttackGoal(this, 2.25D));
        this.goalSelector.addGoal(2, new RandomSwimmingGoal(this, 1.0D, 10));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, false, false,
                entity -> this.hasHuntingCooldown() && entity.getType().is(NaturalistTags.EntityTypes.GREAT_WHITE_SHARK_HOSTILES) && entity.isInWater()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false,
                player -> this.hasHuntingCooldown() && player.isInWater() && this.getLightLevelDependentMagicValue() < 0.5F));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        super.setTarget(target);
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
        if (FOOD_ITEMS.test(stack) && this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            stack.consume(1, player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.parts != null) {
            MobPart.assignIds(this.parts, id);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.partsRegistered && this.level() instanceof MultipartLevel multipart) {
            MobPart.registerAll(multipart, this.parts);
            this.partsRegistered = true;
        }
        this.positionParts();
        MobPart.pushEntities(this, this.parts);
        if (!this.level().isClientSide && MobPart.resolveBodyCollisions(this, this.parts)) {
            this.positionParts();
        }
    }

    @Override
    public void remove(Entity.@NotNull RemovalReason reason) {
        if (this.partsRegistered && this.level() instanceof MultipartLevel multipart) {
            MobPart.unregisterAll(multipart, this.parts);
            this.partsRegistered = false;
        }
        super.remove(reason);
    }

    private void positionParts() {
        float yaw = this.chain.getRenderYaw() * Mth.DEG_TO_RAD;
        float pitch = this.xBodyRot * Mth.DEG_TO_RAD;
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);
        float cosPitch = Mth.cos(pitch);
        float sinPitch = -Mth.sin(pitch);
        for (int i = 0; i < this.parts.length; i++) {
            MobPart part = this.parts[i];
            double z = PART_Z[i];
            double px = this.getX() - sin * z * cosPitch;
            double pz = this.getZ() + cos * z * cosPitch;
            double py = this.getY() + (this.getBbHeight() - part.getBbHeight()) * 0.5D + sinPitch * z;
            part.setOldPosAndRot();
            part.setPos(px, py, pz);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.xBodyRotO = this.xBodyRot;
        float targetPitch = 0.0F;
        Vec3 movement = this.getDeltaMovement();
        if (this.isInWater() && movement.horizontalDistance() > 0.01D) {
            targetPitch = -((float) (Mth.atan2(movement.y, movement.horizontalDistance()) * Mth.RAD_TO_DEG));
            targetPitch = Mth.clamp(targetPitch, -MAX_TILT, MAX_TILT);
        }
        this.xBodyRot += (targetPitch - this.xBodyRot) * 0.1F;
        this.chain.tick(this.yBodyRot, this.xBodyRot, targetPitch);
        if (!this.level().isClientSide) {
            this.tickHuntingCooldown();
            this.flopCooldown = BeachedMob.tickFlopping(this, this.flopCooldown, null);
        }
    }

    @Override
    protected @NotNull AABB getAttackBoundingBox() {
        return super.getAttackBoundingBox().inflate(0.9D, 0.5D, 0.9D);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        boolean aggressive = this.getTarget() != null;
        this.setAggressive(aggressive);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            if (aggressive && !speed.hasModifier(AGGRO_SPEED_BOOST_ID)) {
                speed.addTransientModifier(AGGRO_SPEED_BOOST);
            } else if (!aggressive && speed.hasModifier(AGGRO_SPEED_BOOST_ID)) {
                speed.removeModifier(AGGRO_SPEED_BOOST_ID);
            }
        }
    }

    public boolean isFacing(LivingEntity target) {
        Vec3 toTarget = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
                .subtract(this.getX(), this.getY() + this.getBbHeight() * 0.5D, this.getZ());
        if (toTarget.lengthSqr() < 1.0E-4D) {
            return true;
        }
        Vec3 facing = Vec3.directionFromRotation(this.xBodyRot, this.chain.getRenderYaw());
        return facing.dot(toTarget.normalize()) >= ATTACK_CONE_COS;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.GREAT_WHITE_SHARK_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.GREAT_WHITE_SHARK_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.GREAT_WHITE_SHARK_DEATH.get();
    }

    static class SharkAttackGoal extends Goal {
        private static final double MIN_CHARGE_DIST_SQR = 25.0D;
        private static final double FUMBLE_DIST_SQR = 6.25D;
        private static final double RETREAT_SPEED = 1.2D;
        private static final int RETREAT_TIME_LIMIT = 60;
        private static final int FUMBLE_TIME_LIMIT = 15;

        private final GreatWhiteShark shark;
        private final double chargeSpeed;
        private boolean charging;
        private int retreatTicks;
        private int fumbleTicks;
        private int pathRecalcTicks;

        SharkAttackGoal(GreatWhiteShark shark, double chargeSpeed) {
            this.shark = shark;
            this.chargeSpeed = chargeSpeed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.shark.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            LivingEntity target = this.shark.getTarget();
            if (target == null || this.shark.distanceToSqr(target) >= MIN_CHARGE_DIST_SQR) {
                this.startCharge();
            } else {
                this.startRetreat(target);
            }
        }

        @Override
        public void stop() {
            this.charging = false;
            this.shark.getNavigation().stop();
        }

        @Override
        public void tick() {
            LivingEntity target = this.shark.getTarget();
            if (target == null) {
                return;
            }
            if (this.charging) {
                this.tickCharge(target);
            } else {
                this.tickRetreat(target);
            }
        }

        private void tickCharge(LivingEntity target) {
            this.shark.getLookControl().setLookAt(target, 30.0F, 30.0F);
            if (--this.pathRecalcTicks <= 0) {
                this.pathRecalcTicks = 4;
                this.shark.getNavigation().moveTo(target, this.chargeSpeed);
            }
            if (this.shark.isWithinMeleeAttackRange(target) && this.shark.getSensing().hasLineOfSight(target) && this.shark.isFacing(target)) {
                this.shark.swing(InteractionHand.MAIN_HAND);
                this.shark.doHurtTarget(target);
                this.startRetreat(target);
            } else if (this.shark.distanceToSqr(target) < FUMBLE_DIST_SQR) {
                if (++this.fumbleTicks > FUMBLE_TIME_LIMIT) {
                    this.startRetreat(target);
                }
            } else {
                this.fumbleTicks = 0;
            }
        }

        private void tickRetreat(LivingEntity target) {
            this.retreatTicks++;
            if (this.shark.distanceToSqr(target) >= MIN_CHARGE_DIST_SQR
                    || this.shark.getNavigation().isDone()
                    || this.retreatTicks > RETREAT_TIME_LIMIT) {
                this.startCharge();
            }
        }

        private void startCharge() {
            this.charging = true;
            this.fumbleTicks = 0;
            this.pathRecalcTicks = 0;
        }

        private void startRetreat(LivingEntity target) {
            Vec3 retreatPos = DefaultRandomPos.getPosAway(this.shark, 10, 4, target.position());
            if (retreatPos == null) {
                this.startCharge();
                return;
            }
            this.charging = false;
            this.retreatTicks = 0;
            this.shark.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, RETREAT_SPEED);
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends GreatWhiteShark> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (!this.isInWater()) {
            event.getController().setAnimation(FLOP);
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 || event.isMoving()) {
            event.getController().setAnimation(this.isAggressive() ? SWIM_FAST : SWIM);
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    protected <E extends GreatWhiteShark> @NotNull PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<GreatWhiteShark> event) {
        GreatWhiteShark shark = event.getAnimatable();
        if (!shark.level().isClientSide) {
            return;
        }
        SoundEvent sound = switch (event.getKeyframeData().getSound()) {
            case "swim" -> NaturalistSoundEvents.GREAT_WHITE_SHARK_SWIM.get();
            case "swim_fast" -> NaturalistSoundEvents.GREAT_WHITE_SHARK_SWIM_FAST.get();
            case "attack" -> NaturalistSoundEvents.GREAT_WHITE_SHARK_ATTACK.get();
            case "flop" -> NaturalistSoundEvents.GREAT_WHITE_SHARK_FLOP.get();
            default -> null;
        };
        if (sound != null) {
            shark.level().playLocalSound(shark.getX(), shark.getY(), shark.getZ(), sound, shark.getSoundSource(), 1.0F, 1.0F, false);
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate)
                .setSoundKeyframeHandler(this::soundListener));
    }

    public float getXBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.xBodyRotO, this.xBodyRot);
    }

    public float getRenderYaw(float partialTick) {
        return this.chain.getRenderYaw(partialTick);
    }

    public float getZBodyRot(float partialTick) {
        return this.chain.getRoll(partialTick);
    }

    public float getSegYawOffset(int index, float partialTick) {
        return this.chain.getSegYawOffset(index, partialTick);
    }

    public float getSegPitchOffset(int index, float partialTick) {
        return this.chain.getSegPitchOffset(index, partialTick, this.getXBodyRot(partialTick));
    }
    //endregion
}
