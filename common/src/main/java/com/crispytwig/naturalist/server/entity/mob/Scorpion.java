package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.registry.NaturalistRegistry;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.base.NocturnalHostile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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

public abstract class Scorpion extends Animal implements NaturalistGeoEntity, NocturnalHostile {
    //region Data
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private final RawAnimation idleAnim;
    private final RawAnimation walkAnim;
    private final RawAnimation runAnim;
    private final RawAnimation attackAnim;

    protected Scorpion(EntityType<? extends Animal> entityType, Level level, RawAnimation idleAnim, RawAnimation walkAnim, RawAnimation runAnim, RawAnimation attackAnim) {
        super(entityType, level);
        this.idleAnim = idleAnim;
        this.walkAnim = walkAnim;
        this.runAnim = runAnim;
        this.attackAnim = attackAnim;
    }
    //endregion

    //region Spawning
    public static boolean checkScorpionSpawnRules(EntityType<? extends Animal> entityType, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(NaturalistTags.BlockTags.SCORPIONS_SPAWNABLE_ON) && isBrightEnoughToSpawn(level, pos);
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
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.35D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, true,
                entity -> entity.getType().is(NaturalistTags.EntityTypes.SCORPION_HOSTILES)));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, true,
                player -> this.canHuntPlayers() && this.isInDarkness()));
    }

    protected boolean canHuntPlayers() {
        return true;
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Player && this.level().getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        super.setTarget(target);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        boolean result = super.doHurtTarget(entity);
        if (result && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 120), this);
        }
        return result;
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(NaturalistRegistry.LIZARD_TAIL.get()) && this.getHealth() < this.getMaxHealth()) {
            this.heal(1.0F);
            this.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
            stack.consume(1, player);
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.setAggressive(this.getTarget() != null);
    }

    @Override
    protected float getSoundVolume() {
        return 0.7F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.SCORPION_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.SCORPION_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.SCORPION_DEATH.get();
    }
    //endregion

    //region Animation
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    protected <E extends Scorpion> @NotNull PlayState predicate(final AnimationState<E> event) {
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            if (this.isAggressive()) {
                event.getController().setAnimation(this.runAnim);
                event.getController().setAnimationSpeed(1.6F);
            } else {
                event.getController().setAnimation(this.walkAnim);
                event.getController().setAnimationSpeed(2.0F);
            }
        } else {
            event.getController().setAnimation(this.idleAnim);
            event.getController().setAnimationSpeed(1.0F);
        }
        return PlayState.CONTINUE;
    }

    protected <E extends Scorpion> @NotNull PlayState attackPredicate(final AnimationState<E> event) {
        if (this.swinging) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(this.attackAnim);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Scorpion> event) {
        Scorpion scorpion = event.getAnimatable();
        if (!scorpion.level().isClientSide) {
            return;
        }
        SoundEvent sound;
        float volume;
        switch (event.getKeyframeData().getSound()) {
            case "attack" -> {
                sound = NaturalistSoundEvents.SCORPION_ATTACK.get();
                volume = scorpion.getSoundVolume();
            }
            case "step" -> {
                sound = NaturalistSoundEvents.SCORPION_STEP.get();
                volume = 0.63F;
            }
            default -> {
                return;
            }
        }
        scorpion.level().playLocalSound(scorpion.getX(), scorpion.getY(), scorpion.getZ(), sound, scorpion.getSoundSource(), volume, scorpion.getVoicePitch(), false);
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate)
                .setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate)
                .setSoundKeyframeHandler(this::soundListener));
    }
    //endregion
}
