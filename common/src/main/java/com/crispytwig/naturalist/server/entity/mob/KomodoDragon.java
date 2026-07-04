package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.ai.goal.BabyHurtByTargetGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.BabyPanicGoal;
import com.crispytwig.naturalist.server.entity.ai.goal.SleepGoal;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.base.SleepingAnimal;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
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

import java.util.List;

public class KomodoDragon extends Animal implements NaturalistGeoEntity, SleepingAnimal {
    //region Data
    private static final Ingredient FOOD_ITEMS = Ingredient.of(NaturalistTags.ItemTags.KOMODO_DRAGON_FOOD_ITEMS);
    private static final int PLAYER_TARGETING_TIME = 400;

    private static final EntityDataAccessor<Boolean> BASKING = SynchedEntityData.defineId(KomodoDragon.class, EntityDataSerializers.BOOLEAN);

    private int playerNearbyTicks;
    private boolean playerTargeting;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.komodo_dragon.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.komodo_dragon.walk");
    protected static final RawAnimation BASK = RawAnimation.begin().thenLoop("animation.sf_nba.komodo_dragon.bask");
    protected static final RawAnimation BITE = RawAnimation.begin().thenPlay("animation.sf_nba.komodo_dragon.bite");
    protected static final RawAnimation BABY_TRANSFORM = RawAnimation.begin().thenLoop("animation.sf_nba.komodo_dragon.baby_transform");

    public KomodoDragon(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BASKING, false);
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(BASKING);
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(BASKING, sleeping);
    }

    public boolean isPlayerTargeting() {
        return this.playerTargeting;
    }
    //endregion

    //region Spawning
    public static boolean checkKomodoDragonSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(NaturalistTags.BlockTags.KOMODO_DRAGONS_SPAWNABLE_ON) && isBrightEnoughToSpawn(level, pos);
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.05F);
        }
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel level, @NotNull AgeableMob mob) {
        return NaturalistEntityTypes.KOMODO_DRAGON.get().create(level);
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.5D, true));
        this.goalSelector.addGoal(2, new BabyPanicGoal(this, 2.0D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(5, new KomodoDragonBaskGoal(this));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new BabyHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, true,
                entity -> entity.getType().is(NaturalistTags.EntityTypes.KOMODO_DRAGON_HOSTILES) && !this.isBaby()));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, true,
                player -> this.isPlayerTargeting() && !this.isBaby()));
    }

    @Override
    public boolean canSleep() {
        return this.level().isDay() && this.getTarget() == null && !this.level().isWaterAt(this.blockPosition());
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean result = super.doHurtTarget(entity);
        if (result && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 100), this);
        }
        return result;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.isFood(stack) && this.getHealth() < this.getMaxHealth()) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            this.heal(2.0F);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
    }

    @Override
    public void customServerAiStep() {
        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 1.05D && this.onGround());
        } else {
            this.setSprinting(false);
        }
        if (this.isBaby()) {
            this.playerNearbyTicks = 0;
            this.playerTargeting = false;
            return;
        }
        if (this.tickCount % 10 == 0) {
            List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D),
                    player -> !player.isCreative() && !player.isSpectator());
            if (players.isEmpty()) {
                this.playerNearbyTicks = 0;
                if (this.getTarget() == null) {
                    this.playerTargeting = false;
                }
            } else if (this.playerNearbyTicks < PLAYER_TARGETING_TIME) {
                this.playerNearbyTicks += 10;
                if (this.playerNearbyTicks >= PLAYER_TARGETING_TIME) {
                    this.playerTargeting = true;
                }
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.KOMODO_DRAGON_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.KOMODO_DRAGON_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.KOMODO_DRAGON_DEATH.get();
    }

    static class KomodoDragonBaskGoal extends SleepGoal<KomodoDragon> {
        private final KomodoDragon komodoDragon;
        private int baskTime;

        public KomodoDragonBaskGoal(KomodoDragon komodoDragon) {
            super(komodoDragon);
            this.komodoDragon = komodoDragon;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && (this.komodoDragon.isSleeping() || this.komodoDragon.getRandom().nextFloat() < 0.001F);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && --this.baskTime > 0;
        }

        @Override
        public void start() {
            super.start();
            this.baskTime = 160 + this.komodoDragon.getRandom().nextInt(340);
        }
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends KomodoDragon> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (this.isSleeping()) {
            event.getController().setAnimation(BASK);
            event.getController().setAnimationSpeed(1.0F);
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            event.getController().setAnimation(WALK);
            event.getController().setAnimationSpeed(this.isSprinting() ? 2.0F : 1.0F);
        } else {
            event.getController().setAnimation(IDLE);
            event.getController().setAnimationSpeed(1.0F);
        }
        return PlayState.CONTINUE;
    }

    protected <E extends KomodoDragon> @NotNull PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(BITE);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    protected <E extends KomodoDragon> @NotNull PlayState babyPredicate(final AnimationState<E> event) {
        if (this.isBaby()) {
            event.getController().setAnimation(BABY_TRANSFORM);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();
        return PlayState.STOP;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<KomodoDragon> event) {
        KomodoDragon komodoDragon = event.getAnimatable();
        if (!komodoDragon.level().isClientSide) {
            return;
        }
        SoundEvent sound;
        float volume;
        switch (event.getKeyframeData().getSound()) {
            case "bite" -> {
                sound = NaturalistSoundEvents.KOMODO_DRAGON_ATTACK.get();
                volume = 1.0F;
            }
            case "step" -> {
                sound = NaturalistSoundEvents.KOMODO_DRAGON_STEP.get();
                volume = 0.4F;
            }
            case "step_-12dB" -> {
                sound = NaturalistSoundEvents.KOMODO_DRAGON_STEP.get();
                volume = 0.15F;
            }
            default -> {
                return;
            }
        }
        komodoDragon.level().playLocalSound(komodoDragon.getX(), komodoDragon.getY(), komodoDragon.getZ(), sound, komodoDragon.getSoundSource(), volume, 1.0F, false);
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate)
                .setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "babyController", 0, this::babyPredicate));
    }
    //endregion
}
