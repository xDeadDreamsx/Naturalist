package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.server.entity.ai.goal.HideGoal;
import com.crispytwig.naturalist.server.entity.base.HidingAnimal;
import com.crispytwig.naturalist.server.entity.base.NaturalistAnimal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.misc.DirtTrail;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
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
import software.bernie.geckolib.animation.keyframe.event.ParticleKeyframeEvent;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class Mole extends NaturalistAnimal implements NaturalistGeoEntity, HidingAnimal, DataDrivenVariantAnimal {
    //region Data
    private static final int STATE_UNROLLED = 0;
    private static final int STATE_DIGGING = 1;
    private static final int STATE_UNDERGROUND = 2;
    private static final int STATE_PEEKING = 3;
    private static final int STATE_UNROLLING = 4;

    private static final int DIG_DOWN_TIME = 20;
    private static final int MOUND_SPAWN_TIME = 13;
    private static final int PEEK_TIME = 44;
    private static final int UNROLL_TIME = 25;
    private static final int CALM_TIME = 60;
    private static final int ATTACKER_MEMORY_TIME = 1200;
    private static final int DIG_LOCKOUT_TIME = 60;
    private static final int DIG_HOLD_TIME = 3;
    private static final int UNDERGROUND_GRACE = 60;

    private static final EntityDataAccessor<String> DATA_VARIANT = SynchedEntityData.defineId(Mole.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_STATE = SynchedEntityData.defineId(Mole.class, EntityDataSerializers.INT);

    private int stateTicks;
    private int calmTicks;
    private int peekTicks;
    private boolean threatDetected;
    private boolean peekSawThreat;
    @Nullable
    private UUID rememberedAttacker;
    private int attackerMemoryTicks;
    private int digLockoutTicks;
    private int dugOutHoldTicks;
    private int undergroundGraceTicks;
    @Nullable
    private BlockPos lastMoundPos;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.mole.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.mole.walk");
    protected static final RawAnimation IDLE_EVENT = RawAnimation.begin().thenPlay("animation.sf_nba.mole.idle_event");
    protected static final RawAnimation DIG_DOWN = RawAnimation.begin().thenPlayAndHold("animation.sf_nba.mole.dig_down");
    protected static final RawAnimation UNDERGROUND_HIDDEN = RawAnimation.begin().thenPlayAndHold("animation.sf_nba.mole.underground_hidden");
    protected static final RawAnimation PEEK = RawAnimation.begin().thenPlay("animation.sf_nba.mole.spawn").thenPlayAndHold("animation.sf_nba.mole.peek");
    protected static final RawAnimation DIG_UP = RawAnimation.begin().thenPlayAndHold("animation.sf_nba.mole.dig_up");

    public Mole(EntityType<? extends NaturalistAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, this.defaultVariant().location().toString());
        builder.define(DATA_STATE, STATE_UNROLLED);
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/mole.png");
    }

    @Override
    public String getVariantRawId() {
        return this.entityData.get(DATA_VARIANT);
    }

    @Override
    public void setVariantRawId(String id) {
        this.entityData.set(DATA_VARIANT, id);
    }

    private int getState() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(int state) {
        this.entityData.set(DATA_STATE, state);
        this.applyStateEffects(state);
        if (state == STATE_DIGGING || state == STATE_UNROLLED) {
            this.gameEvent(GameEvent.ENTITY_ACTION);
        }
        if (state == STATE_UNDERGROUND) {
            this.placeTrailMound();
        }
        if (state == STATE_DIGGING || state == STATE_PEEKING || state == STATE_UNROLLING) {
            BlockPos pos = this.blockPosition();
            this.setPos(pos.getX() + 0.5D, this.getY(), pos.getZ() + 0.5D);
            this.getNavigation().stop();
        }
    }

    private void applyStateEffects(int state) {
        switch (state) {
            case STATE_DIGGING -> this.stateTicks = DIG_DOWN_TIME;
            case STATE_UNDERGROUND -> {
                this.calmTicks = 0;
                this.peekTicks = 100 + this.random.nextInt(300);
                this.lastMoundPos = null;
                this.undergroundGraceTicks = UNDERGROUND_GRACE;
            }
            case STATE_PEEKING -> {
                this.stateTicks = MOUND_SPAWN_TIME + PEEK_TIME;
                this.peekSawThreat = false;
            }
            case STATE_UNROLLING -> this.stateTicks = UNROLL_TIME;
        }
        AttributeInstance stepHeight = this.getAttribute(Attributes.STEP_HEIGHT);
        if (stepHeight != null) {
            stepHeight.setBaseValue(state == STATE_UNROLLED ? 0.6D : 1.0D);
        }
    }

    public boolean isUnrolled() {
        return this.getState() == STATE_UNROLLED;
    }

    public boolean isRolledUp() {
        return !this.isUnrolled();
    }

    @Override
    public boolean canHide() {
        int state = this.getState();
        return state == STATE_DIGGING || state == STATE_PEEKING || state == STATE_UNROLLING;
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveVariant(compound);
        compound.putInt("MoleState", this.getState());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.loadVariant(compound);
        int state = compound.getInt("MoleState");
        this.entityData.set(DATA_STATE, state);
        this.applyStateEffects(state);
    }
    //endregion

    //region Spawning
    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return stack.is(Items.SPIDER_EYE);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        Mole baby = NaturalistEntityTypes.MOLE.get().create(level);
        if (baby != null) {
            baby.setVariantRawId(this.inheritVariantFrom(mob, this.random));
        }
        return baby;
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.pickVariantForSpawn(level);
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.05F);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new HideGoal<>(this));
        this.goalSelector.addGoal(1, new MolePanicGoal(this, 2.0D));
        this.goalSelector.addGoal(2, new MoleFleeGoal(this));
        this.goalSelector.addGoal(3, new MoleScurryGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return Mole.this.isUnrolled() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return Mole.this.isUnrolled() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, Ingredient.of(Items.SPIDER_EYE), false) {
            @Override
            public boolean canUse() {
                return Mole.this.isUnrolled() && super.canUse();
            }
        });
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D) {
            @Override
            public boolean canUse() {
                return Mole.this.isUnrolled() && super.canUse();
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F, 0.02F) {
            @Override
            public boolean canUse() {
                return Mole.this.isUnrolled() && super.canUse();
            }
        });
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return Mole.this.isUnrolled() && super.canUse();
            }
        });
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.attackerMemoryTicks > 0) {
            this.attackerMemoryTicks--;
        }
        if (this.digLockoutTicks > 0) {
            this.digLockoutTicks--;
        }
        if (this.undergroundGraceTicks > 0) {
            this.undergroundGraceTicks--;
        }
        if (this.dugOutHoldTicks > 0) {
            this.dugOutHoldTicks--;
            freezeInPlace(this);
            return;
        }
        if (this.tickCount % 4 == 0) {
            this.threatDetected = this.scanForThreats();
        }
        int state = this.getState();
        if (state == STATE_UNROLLED) {
            if (this.random.nextInt(600) == 0 && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-6) {
                this.triggerAnim("idleEventController", "idle_event");
            }
            if (this.threatDetected) {
                this.tryRollUp();
            }
            return;
        }
        boolean forcedOut = this.isOnFire() || this.isInWater() || this.isLeashed() || this.isPassenger();
        if (forcedOut && state != STATE_UNROLLING) {
            this.setState(STATE_UNROLLING);
            return;
        }
        switch (state) {
            case STATE_DIGGING -> {
                if (--this.stateTicks <= 0) {
                    this.setState(STATE_UNDERGROUND);
                }
            }
            case STATE_UNDERGROUND -> {
                if (this.threatDetected) {
                    this.calmTicks = 0;
                    if (--this.peekTicks <= 0) {
                        this.setState(STATE_PEEKING);
                    }
                } else if (++this.calmTicks >= CALM_TIME) {
                    this.setState(STATE_PEEKING);
                }
                this.leaveDirtTrail();
            }
            case STATE_PEEKING -> {
                if (this.threatDetected) {
                    this.peekSawThreat = true;
                }
                if (--this.stateTicks <= 0) {
                    this.setState(this.peekSawThreat ? STATE_DIGGING : STATE_UNROLLING);
                }
            }
            case STATE_UNROLLING -> {
                if (!forcedOut && this.threatDetected && this.digLockoutTicks <= 0) {
                    this.setState(STATE_UNDERGROUND);
                } else if (--this.stateTicks <= 0) {
                    this.setState(STATE_UNROLLED);
                }
            }
        }
    }

    private boolean scanForThreats() {
        return !this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(7.0D, 2.0D, 7.0D), this::isThreat).isEmpty();
    }

    private boolean isThreat(LivingEntity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        if (entity.getType().is(EntityTypeTags.UNDEAD)) {
            return true;
        }
        if (this.attackerMemoryTicks > 0 && entity.getUUID().equals(this.rememberedAttacker)) {
            return true;
        }
        return entity instanceof Player player && (player.isSprinting() || player.isPassenger());
    }

    private void tryRollUp() {
        if (this.digLockoutTicks > 0 || this.getState() != STATE_UNROLLED || !this.onGround() || this.isOnFire() || this.isFreezing()
                || this.isInWater() || this.isLeashed() || this.isPassenger()) {
            return;
        }
        this.setState(STATE_DIGGING);
    }

    public void holdForDigging() {
        this.dugOutHoldTicks = DIG_HOLD_TIME;
    }

    public boolean isBeingDugOut() {
        return this.dugOutHoldTicks > 0;
    }

    public boolean canBeDugOut() {
        return this.getState() == STATE_UNDERGROUND && this.undergroundGraceTicks <= 0;
    }

    public void popOut(LivingEntity digger) {
        this.rememberedAttacker = digger.getUUID();
        this.attackerMemoryTicks = ATTACKER_MEMORY_TIME;
        this.digLockoutTicks = DIG_LOCKOUT_TIME;
        this.dugOutHoldTicks = 0;
        int state = this.getState();
        if (state != STATE_UNROLLING && state != STATE_UNROLLED) {
            this.setState(STATE_UNROLLING);
        }
    }

    private void leaveDirtTrail() {
        if (this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4) {
            return;
        }
        this.placeTrailMound();
    }

    private void placeTrailMound() {
        if (!this.onGround()) {
            return;
        }
        BlockPos pos = this.blockPosition();
        if (pos.equals(this.lastMoundPos)) {
            return;
        }
        this.lastMoundPos = pos;
        BlockPos below = pos.below();
        if (!this.level().getBlockState(below).isFaceSturdy(this.level(), below, Direction.UP)
                || !this.level().getBlockState(pos).getCollisionShape(this.level(), pos).isEmpty()) {
            return;
        }
        if (!this.level().getEntitiesOfClass(DirtTrail.class, new AABB(pos)).isEmpty()) {
            return;
        }
        DirtTrail trail = NaturalistEntityTypes.DIRT_TRAIL.get().create(this.level());
        if (trail == null) {
            return;
        }
        trail.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        trail.setSmall(this.isBaby());
        this.level().addFreshEntity(trail);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        boolean peeking = this.getState() == STATE_PEEKING;
        if (this.isRolledUp() && !peeking && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            if (peeking) {
                this.popOut(attacker);
            } else {
                this.rememberedAttacker = attacker.getUUID();
                this.attackerMemoryTicks = ATTACKER_MEMORY_TIME;
                this.tryRollUp();
            }
        }
        return hurt;
    }

    @Override
    public boolean isPushable() {
        return !this.isRolledUp() && super.isPushable();
    }

    @Override
    public void jumpFromGround() {
        if (!this.isRolledUp()) {
            super.jumpFromGround();
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (this.isRolledUp()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!this.level().isClientSide) {
                this.usePlayerItem(player, hand, stack);
                this.heal(2.0F);
                this.playSound(this.getEatingSound(stack), 1.0F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isRolledUp() ? null : NaturalistSoundEvents.MOLE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource source) {
        return NaturalistSoundEvents.MOLE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.MOLE_DEATH.get();
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
    }

    private static class MolePanicGoal extends PanicGoal {
        public MolePanicGoal(Mole mole, double speedModifier) {
            super(mole, speedModifier);
        }

        @Override
        protected boolean shouldPanic() {
            return this.mob.isOnFire() || this.mob.isFreezing();
        }
    }

    private static class MoleScurryGoal extends RandomStrollGoal {
        private final Mole mole;

        public MoleScurryGoal(Mole mole) {
            super(mole, 1.5D, 1);
            this.mole = mole;
        }

        @Override
        public boolean canUse() {
            return !this.mole.isBeingDugOut() && this.mole.getState() == STATE_UNDERGROUND && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mole.isBeingDugOut() && this.mole.getState() == STATE_UNDERGROUND && super.canContinueToUse();
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            return DefaultRandomPos.getPos(this.mob, 5, 4);
        }
    }

    private static class MoleFleeGoal extends AvoidEntityGoal<LivingEntity> {
        private final Mole mole;

        public MoleFleeGoal(Mole mole) {
            super(mole, LivingEntity.class, entity -> !(entity instanceof Mole) && (entity instanceof Player || entity instanceof Mob), 6.0F, 1.0D, 1.5D, entity -> true);
            this.mole = mole;
        }

        @Override
        public boolean canUse() {
            return !this.mole.isBeingDugOut() && this.mole.getState() == STATE_UNDERGROUND && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mole.isBeingDugOut() && this.mole.getState() == STATE_UNDERGROUND && super.canContinueToUse();
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Mole> @NotNull PlayState predicate(final AnimationState<E> event) {
        switch (this.getState()) {
            case STATE_DIGGING -> event.getController().setAnimation(DIG_DOWN);
            case STATE_UNDERGROUND -> event.getController().setAnimation(UNDERGROUND_HIDDEN);
            case STATE_PEEKING -> event.getController().setAnimation(PEEK);
            case STATE_UNROLLING -> event.getController().setAnimation(DIG_UP);
            default -> event.getController().setAnimation(event.isMoving() ? WALK : IDLE);
        }
        if (this.getState() == STATE_UNROLLED && event.isMoving()) {
            event.getController().setAnimationSpeed(this.movementAnimationSpeed(event, 1.0D));
        } else {
            event.getController().setAnimationSpeed(1.0D);
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Mole> event) {
        Mole mole = event.getAnimatable();
        if (!mole.level().isClientSide) {
            return;
        }
        SoundEvent sound;
        float volume;
        switch (event.getKeyframeData().getSound()) {
            case "dig_down" -> {
                sound = NaturalistSoundEvents.MOLE_DIG_DOWN.get();
                volume = 0.7F;
            }
            case "dig_up" -> {
                sound = NaturalistSoundEvents.MOLE_DIG_UP.get();
                volume = 0.7F;
            }
            case "peek" -> {
                sound = NaturalistSoundEvents.MOLE_PEEK.get();
                volume = 0.7F;
            }
            case "step_-6dB" -> {
                sound = NaturalistSoundEvents.MOLE_STEP.get();
                volume = 0.25F;
            }
            case "step_-12dB" -> {
                sound = NaturalistSoundEvents.MOLE_STEP.get();
                volume = 0.15F;
            }
            default -> {
                return;
            }
        }
        mole.level().playLocalSound(mole.getX(), mole.getY(), mole.getZ(), sound, mole.getSoundSource(), volume, 1.0F, false);
    }

    private void particleListener(@NotNull ParticleKeyframeEvent<Mole> event) {
        Mole mole = event.getAnimatable();
        Level level = mole.level();
        if (!level.isClientSide) {
            return;
        }
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState());
        for (int i = 0; i < 16; i++) {
            level.addParticle(particle, mole.getRandomX(0.75D), mole.getY() + 0.1D, mole.getRandomZ(0.75D),
                    mole.random.nextGaussian() * 0.05D, 0.1D + mole.random.nextFloat() * 0.15D, mole.random.nextGaussian() * 0.05D);
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new SmoothSpeedAnimationController<>(this, "controller", 4, this::predicate)
                .setSoundKeyframeHandler(this::soundListener)
                .setParticleKeyframeHandler(this::particleListener));
        controllers.add(new AnimationController<>(this, "idleEventController", 0, event -> PlayState.STOP)
                .triggerableAnim("idle_event", IDLE_EVENT));
    }
    //endregion
}
