package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.server.entity.ai.goal.WhaleDiveGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.WhaleSeekDeeperWaterGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.WhaleSurfaceGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.WhaleSwimGoal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.util.MultipartLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
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
public class Whale extends Animal implements NaturalistGeoEntity {
    //region Data
    private static final float MAX_TILT = 40.0F;
    private static final float BREACH_TILT = 60.0F;
    private static final float MAX_ROLL = 30.0F;
    private static final float ROLL_PER_YAW = 5.0F;
    private static final float ROLL_EASE = 0.06F;
    private static final int SEGMENTS = 4;
    private static final int BODY_PUSH_BAIL = 20;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int blowholeCooldown;
    private boolean diving;
    private int bodyPushTicks;
    private int flopCooldown;

    private final WhalePart[] parts;
    private boolean partsRegistered;

    public float xBodyRot;
    public float xBodyRotO;
    private float renderYaw;
    private float renderYawO;
    private float zBodyRot;
    private float zBodyRotO;
    private float finLag;
    private float finLagO;
    private float frontDroop;
    private float frontDroopO;
    private float backDroop;
    private float backDroopO;
    private final float[] segYaw = new float[SEGMENTS];
    private final float[] segYawO = new float[SEGMENTS];
    private final float[] segPitch = new float[SEGMENTS];
    private final float[] segPitchO = new float[SEGMENTS];
    private boolean chainsInitialized;

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.whale.idle");
    protected static final RawAnimation SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.whale.swim");
    protected static final RawAnimation FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.whale.flop");
    protected static final RawAnimation BABY_IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.whale_baby.swim_idle");
    protected static final RawAnimation BABY_SWIM = RawAnimation.begin().thenLoop("animation.sf_nba.whale_baby.swim");
    protected static final RawAnimation BABY_FLOP = RawAnimation.begin().thenLoop("animation.sf_nba.whale_baby.flop");

    public Whale(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new WhaleMoveControl(this, 45, 4, 0.02F, 0.1F);
        this.lookControl = new SmoothSwimmingLookControl(this, 6);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.parts = new WhalePart[WhalePart.PART_COUNT];
        for (int i = 0; i < this.parts.length; i++) {
            this.parts[i] = new WhalePart(this, i);
        }
    }

    public WhalePart[] getParts() {
        return this.parts;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!this.level().isClientSide && this.isAlive() && this.isInWater()) {
            this.setAirSupply(this.getMaxAirSupply());
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
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (source.getEntity() instanceof Jellyfish || source.getEntity() instanceof Pufferfish) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.9F;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.WHALE_AMBIENT.get();
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.WHALE_HURT.get();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return NaturalistSoundEvents.WHALE_DEATH.get();
    }
    //endregion

    //region Spawning
    public static boolean checkWhaleSpawnRules(EntityType<Whale> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getFluidState(pos.above(1)).is(FluidTags.WATER)
                && level.getFluidState(pos.above(2)).is(FluidTags.WATER);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        AgeableMobGroupData groupData;
        if (spawnData == null) {
            spawnData = new AgeableMobGroupData(false);
        }
        if ((groupData = (AgeableMobGroupData) spawnData).getGroupSize() > 0) {
            this.setAge(-24000);
        }
        groupData.increaseGroupSizeByOne();
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return stack.is(Items.COD) || stack.is(Items.COOKED_COD)
                || stack.is(Items.SALMON) || stack.is(Items.COOKED_SALMON)
                || stack.is(NaturalistRegistry.CRAB_MEAT.get()) || stack.is(NaturalistRegistry.COOKED_CRAB_MEAT.get());
    }

    @Override
    public boolean canMate(@NotNull Animal otherAnimal) {
        return this.isInWater() && otherAnimal.isInWater() && super.canMate(otherAnimal);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.WHALE.get().create(serverLevel);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(1, new WhaleSeekDeeperWaterGoal(this));
        this.goalSelector.addGoal(2, new WhaleSurfaceGoal(this));
        this.goalSelector.addGoal(3, new WhaleDiveGoal(this));
        this.goalSelector.addGoal(4, new WhaleSwimGoal(this, 1.0D, 2));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isEffectiveAi() && this.isInWater()) {
            this.moveRelative(this.getSpeed(), travelVector);
            if (travelVector.y != 0.0D) {
                Vec3 dm = this.getDeltaMovement();
                this.setDeltaMovement(dm.x, Mth.lerp(0.25D, dm.y, travelVector.y * this.getSpeed() * 10.0D), dm.z);
            }
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(travelVector);
            if (!this.isInWater() && this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 1.0D, 0.3D));
            }
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide) {
                this.usePlayerItem(player, hand, stack);
                this.heal(4.0F);
                this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        if (this.parts != null) {
            for (int i = 0; i < this.parts.length; i++) {
                this.parts[i].setId(id + 1 + i);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.partsRegistered && this.level() instanceof MultipartLevel multipart) {
            for (WhalePart part : this.parts) {
                multipart.naturalist$addWhalePart(part);
            }
            this.partsRegistered = true;
        }
        this.positionParts();
        this.pushEntitiesFromParts();
        if (!this.level().isClientSide) {
            this.resolveBodyCollisions();
        }
    }

    private void pushEntitiesFromParts() {
        for (WhalePart part : this.parts) {
            List<Entity> list = this.level().getEntities(part, part.getBoundingBox(),
                    e -> !e.is(this) && !(e instanceof WhalePart) && e.isPushable());
            for (Entity entity : list) {
                part.push(entity);
            }
        }
    }

    @Override
    public void remove(Entity.@NotNull RemovalReason reason) {
        if (this.partsRegistered && this.level() instanceof MultipartLevel multipart) {
            for (WhalePart part : this.parts) {
                multipart.naturalist$removeWhalePart(part);
            }
            this.partsRegistered = false;
        }
        super.remove(reason);
    }

    private void positionParts() {
        float yaw = this.renderYaw * Mth.DEG_TO_RAD;
        float pitch = this.xBodyRot * Mth.DEG_TO_RAD;
        float sin = Mth.sin(yaw);
        float cos = Mth.cos(yaw);
        float cosPitch = Mth.cos(pitch);
        float sinPitch = -Mth.sin(pitch);
        float scale = this.isBaby() ? 0.5F : 1.0F;
        for (int i = 0; i < this.parts.length; i++) {
            WhalePart part = this.parts[i];
            part.updateScale(scale);
            double z = WhalePart.PART_Z[i] * scale;
            double px = this.getX() - sin * z * cosPitch;
            double pz = this.getZ() + cos * z * cosPitch;
            double py = this.getY() + (this.getBbHeight() - part.getBbHeight()) * 0.5D + sinPitch * z;
            double zRaw = WhalePart.PART_Z[i];
            double droopRatio = zRaw / (zRaw > 0.0D ? WhalePart.PART_Z[0] : WhalePart.PART_Z[WhalePart.PART_COUNT - 1]);
            py -= (zRaw > 0.0D ? this.frontDroop : this.backDroop) * droopRatio * droopRatio * 0.9D * scale;
            part.setOldPosAndRot();
            part.setPos(px, py, pz);
        }
    }

    private void resolveBodyCollisions() {
        Vec3 push = Vec3.ZERO;
        Vec3 center = this.position().add(0.0D, this.getBbHeight() * 0.5D, 0.0D);
        for (WhalePart part : this.parts) {
            if (!this.level().getBlockCollisions(part, part.getBoundingBox()).iterator().hasNext()) {
                continue;
            }
            Vec3 away = center.subtract(part.position().add(0.0D, part.getBbHeight() * 0.5D, 0.0D));
            if (away.lengthSqr() < 1.0E-4D) {
                continue;
            }
            push = push.add(away.normalize().scale(0.04D));
        }
        if (push.lengthSqr() > 0.0D) {
            this.bodyPushTicks = Math.min(this.bodyPushTicks + 2, 2 * BODY_PUSH_BAIL);
            if (push.length() > 0.12D) {
                push = push.normalize().scale(0.12D);
            }
            this.move(MoverType.SELF, push);
            this.setDeltaMovement(this.getDeltaMovement().add(push.scale(0.2D)));
            this.positionParts();
        } else if (this.bodyPushTicks > 0) {
            this.bodyPushTicks--;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.chainsInitialized) {
            this.chainsInitialized = true;
            this.renderYaw = this.renderYawO = this.yBodyRot;
            for (int i = 0; i < SEGMENTS; i++) {
                this.segYaw[i] = this.segYawO[i] = this.yBodyRot;
            }
        }
        this.xBodyRotO = this.xBodyRot;
        this.renderYawO = this.renderYaw;
        this.zBodyRotO = this.zBodyRot;
        this.finLagO = this.finLag;
        this.frontDroopO = this.frontDroop;
        this.backDroopO = this.backDroop;
        System.arraycopy(this.segYaw, 0, this.segYawO, 0, SEGMENTS);
        System.arraycopy(this.segPitch, 0, this.segPitchO, 0, SEGMENTS);

        float targetPitch = 0.0F;
        Vec3 movement = this.getDeltaMovement();
        boolean grounded = this.onGround() || this.verticalCollisionBelow;
        if (!grounded && movement.horizontalDistanceSqr() > 1.0E-7
                && (this.isInWater() || movement.lengthSqr() > 0.03D)) {
            targetPitch = -((float) (Mth.atan2(movement.y, movement.horizontalDistance()) * Mth.RAD_TO_DEG));
            float maxTilt = this.isInWater() ? MAX_TILT : BREACH_TILT;
            targetPitch = Mth.clamp(targetPitch, -maxTilt, maxTilt);
        }
        this.xBodyRot += (targetPitch - this.xBodyRot) * (grounded ? 0.25F : 0.07F);

        this.renderYaw += Mth.wrapDegrees(this.yBodyRot - this.renderYaw) * 0.15F;
        this.segYaw[0] += Mth.wrapDegrees(this.yBodyRot - this.segYaw[0]) * 0.35F;
        this.segYaw[1] += Mth.wrapDegrees(this.renderYaw - this.segYaw[1]) * 0.14F;
        this.segYaw[2] += Mth.wrapDegrees(this.segYaw[1] - this.segYaw[2]) * 0.11F;
        this.segYaw[3] += Mth.wrapDegrees(this.segYaw[2] - this.segYaw[3]) * 0.09F;

        this.segPitch[0] += (targetPitch - this.segPitch[0]) * 0.24F;
        this.segPitch[1] += (this.xBodyRot - this.segPitch[1]) * 0.12F;
        this.segPitch[2] += (this.segPitch[1] - this.segPitch[2]) * 0.1F;
        this.segPitch[3] += (this.segPitch[2] - this.segPitch[3]) * 0.08F;

        this.finLag += (this.xBodyRot - this.finLag) * 0.1F;

        this.frontDroop += ((this.isInWater() && this.isPartInWater(this.parts[0]) ? 1.0F : 0.0F) - this.frontDroop) * 0.08F;
        this.backDroop += ((this.isInWater() && this.isPartInWater(this.parts[3]) ? 1.0F : 0.0F) - this.backDroop) * 0.08F;

        float rollTarget = Mth.clamp(-Mth.wrapDegrees(this.renderYaw - this.renderYawO) * ROLL_PER_YAW, -MAX_ROLL, MAX_ROLL);
        this.zBodyRot += (rollTarget - this.zBodyRot) * ROLL_EASE;

        if (!this.level().isClientSide) {
            if (this.blowholeCooldown > 0) {
                this.blowholeCooldown--;
            } else if (this.canSpray()) {
                this.spray();
                this.blowholeCooldown = 160 + this.random.nextInt(160);
            }
            if (!this.isInWater() && this.onGround() && this.verticalCollisionBelow) {
                if (this.flopCooldown > 0) {
                    this.flopCooldown--;
                } else {
                    this.flopTowardWater();
                    this.flopCooldown = 15 + this.random.nextInt(25);
                }
            }
        }
    }

    private void flopTowardWater() {
        Vec3 dir = this.findNearestWaterDirection();
        double dx;
        double dz;
        if (dir != null) {
            Vec3 jittered = dir.yRot((this.random.nextFloat() - 0.5F) * 0.35F);
            dx = jittered.x * 0.45D;
            dz = jittered.z * 0.45D;
            float yaw = (float) (Mth.atan2(jittered.z, jittered.x) * Mth.RAD_TO_DEG) - 90.0F;
            this.setYRot(yaw);
            this.yBodyRot = yaw;
            this.yHeadRot = yaw;
        } else {
            dx = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3D;
            dz = (this.random.nextFloat() * 2.0F - 1.0F) * 0.3D;
        }
        this.setDeltaMovement(this.getDeltaMovement().add(dx, 0.5D, dz));
        this.setOnGround(false);
        this.hasImpulse = true;
        this.playSound(SoundEvents.GUARDIAN_FLOP, 2.0F, 0.6F);
    }

    @Nullable
    private Vec3 findNearestWaterDirection() {
        BlockPos origin = this.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-16, -8, -16), origin.offset(16, 1, 16))) {
            if (this.isWaterAt(pos)) {
                double dist = pos.distSqr(origin);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = pos.immutable();
                }
            }
        }
        if (best == null) {
            return null;
        }
        Vec3 dir = new Vec3(best.getX() + 0.5D - this.getX(), 0.0D, best.getZ() + 0.5D - this.getZ());
        return dir.lengthSqr() < 1.0E-4D ? null : dir.normalize();
    }

    public boolean canSpray() {
        return this.isInWater() && this.level().getFluidState(BlockPos.containing(this.getX(), this.getY() + this.getBbHeight() + 1.0D, this.getZ())).isEmpty();
    }

    private void spray() {
        Vec3 forward = this.getForward();
        double reach = this.isBaby() ? 1.0D : 2.0D;
        double sx = this.getX() + forward.x * reach;
        double sy = this.getY() + this.getBbHeight() * 0.9D;
        double sz = this.getZ() + forward.z * reach;
        ServerLevel serverLevel = (ServerLevel) this.level();
        serverLevel.sendParticles(ParticleTypes.CLOUD, sx, sy, sz, 14, 0.15D, 0.35D, 0.15D, 0.12D);
        serverLevel.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, sx, sy, sz, 8, 0.15D, 0.25D, 0.15D, 0.0D);
        this.playSound(NaturalistSoundEvents.WHALE_BLOWHOLE.get(), 1.2F, 1.0F);
    }

    public float getXBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.xBodyRotO, this.xBodyRot);
    }

    public float getRenderYaw(float partialTick) {
        return Mth.rotLerp(partialTick, this.renderYawO, this.renderYaw);
    }

    public float getZBodyRot(float partialTick) {
        return Mth.lerp(partialTick, this.zBodyRotO, this.zBodyRot);
    }

    public float getFinLag(float partialTick) {
        return Mth.lerp(partialTick, this.finLagO, this.finLag);
    }

    public float getFrontDroop(float partialTick) {
        return Mth.lerp(partialTick, this.frontDroopO, this.frontDroop);
    }

    public float getBackDroop(float partialTick) {
        return Mth.lerp(partialTick, this.backDroopO, this.backDroop);
    }

    public boolean isWaterAt(BlockPos pos) {
        return this.level().getFluidState(pos).is(FluidTags.WATER);
    }

    public boolean isDiving() {
        return this.diving;
    }

    public void setDiving(boolean diving) {
        this.diving = diving;
    }

    public boolean isGrindingTerrain() {
        return this.bodyPushTicks >= BODY_PUSH_BAIL;
    }

    private boolean isPartInWater(WhalePart part) {
        return !this.isWaterAt(BlockPos.containing(part.getX(), part.getY() + part.getBbHeight() * 0.5D, part.getZ()));
    }

    public float getSegYawOffset(int index, float partialTick) {
        float current = Mth.rotLerp(partialTick, this.segYawO[index], this.segYaw[index]);
        float reference = index <= 1
                ? this.getRenderYaw(partialTick)
                : Mth.rotLerp(partialTick, this.segYawO[index - 1], this.segYaw[index - 1]);
        return Mth.wrapDegrees(current - reference);
    }

    public float getSegPitchOffset(int index, float partialTick) {
        float current = Mth.lerp(partialTick, this.segPitchO[index], this.segPitch[index]);
        float reference = index <= 1
                ? this.getXBodyRot(partialTick)
                : Mth.lerp(partialTick, this.segPitchO[index - 1], this.segPitch[index - 1]);
        return current - reference;
    }

    static class WhaleMoveControl extends MoveControl {
        private final int maxTurnX;
        private final int maxTurnY;
        private final float inWaterSpeedModifier;
        private final float outsideWaterSpeedModifier;

        WhaleMoveControl(Whale whale, int maxTurnX, int maxTurnY, float inWaterSpeedModifier, float outsideWaterSpeedModifier) {
            super(whale);
            this.maxTurnX = maxTurnX;
            this.maxTurnY = maxTurnY;
            this.inWaterSpeedModifier = inWaterSpeedModifier;
            this.outsideWaterSpeedModifier = outsideWaterSpeedModifier;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO) {
                this.mob.setSpeed(0.0F);
                this.mob.setXxa(0.0F);
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
                return;
            }
            this.operation = Operation.WAIT;
            double dx = this.wantedX - this.mob.getX();
            double dy = this.wantedY - this.mob.getY();
            double dz = this.wantedZ - this.mob.getZ();
            if (dx * dx + dy * dy + dz * dz < 2.5000003E-7D) {
                this.mob.setZza(0.0F);
                return;
            }
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F, this.maxTurnY));
            this.mob.yBodyRot = this.mob.getYRot();
            this.mob.yHeadRot = this.mob.getYRot();
            float speed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
            if (this.mob.isInWater()) {
                this.mob.setSpeed(speed * this.inWaterSpeedModifier);
                double horizontal = Math.sqrt(dx * dx + dz * dz);
                float pitch = 0.0F;
                if (Math.abs(dy) > 1.0E-5D || Math.abs(horizontal) > 1.0E-5D) {
                    pitch = (float) -(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG);
                    pitch = Mth.clamp(Mth.wrapDegrees(pitch), -this.maxTurnX, this.maxTurnX);
                    this.mob.setXRot(this.rotlerp(this.mob.getXRot(), pitch, 5.0F));
                }
                this.mob.zza = Mth.cos(pitch * Mth.DEG_TO_RAD) * speed;
                this.mob.yya = -Mth.sin(pitch * Mth.DEG_TO_RAD) * speed;
            } else {
                this.mob.setSpeed(speed * this.outsideWaterSpeedModifier);
            }
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Whale> @NotNull PlayState predicate(final AnimationState<E> event) {
        AnimationController<E> controller = event.getController();
        boolean baby = this.isBaby();
        if (!this.isInWater()) {
            controller.setAnimation(baby ? BABY_FLOP : FLOP);
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6 || event.isMoving()) {
            controller.setAnimation(baby ? BABY_SWIM : SWIM);
        } else {
            controller.setAnimation(baby ? BABY_IDLE : IDLE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }
    //endregion
}
