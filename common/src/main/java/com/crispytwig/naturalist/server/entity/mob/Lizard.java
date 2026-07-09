package com.crispytwig.naturalist.server.entity.mob;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistMobVariants;
import com.crispytwig.naturalist.registry.NaturalistSoundEvents;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.entity.ai.goal.PetFollowOwnerGoal;
import com.crispytwig.naturalist.server.entity.base.DyeableAnimal;
import com.crispytwig.naturalist.server.entity.base.FollowingPet;
import com.crispytwig.naturalist.server.entity.base.NaturalistGeoEntity;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.crispytwig.naturalist.server.entity.variant.MobVariant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class Lizard extends TamableAnimal implements NaturalistGeoEntity, DyeableAnimal, FollowingPet, DataDrivenVariantAnimal {
    //region Data
    public static final String[] VARIANT_NAMES = {"green", "brown", "beardie", "leopard_gecko"};

    private static final String DEFAULT_VARIANT_ID = "naturalist:green";

    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(NaturalistTags.ItemTags.LIZARD_TEMPT_ITEMS);
    private static final EntityDataAccessor<String> VARIANT_ID = SynchedEntityData.defineId(Lizard.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> HAS_TAIL = SynchedEntityData.defineId(Lizard.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DYE = SynchedEntityData.defineId(Lizard.class, EntityDataSerializers.INT);

    private boolean followingOwner = true;
    private int tailRegrowCooldown = 0;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.lizard.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.lizard.walk");
    protected static final RawAnimation SIT = RawAnimation.begin().thenLoop("animation.sf_nba.lizard.sit");

    public Lizard(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT_ID, DEFAULT_VARIANT_ID);
        builder.define(HAS_TAIL, true);
        builder.define(DATA_DYE, -1);
    }

    @Override
    public ResourceKey<MobVariant> defaultVariant() {
        return NaturalistMobVariants.createKey(NaturalistMobVariants.registryFor("lizard"), "green");
    }

    @Override
    public String[] legacyVariantNames() {
        return VARIANT_NAMES;
    }

    @Override
    public ResourceLocation fallbackVariantTexture() {
        return Naturalist.location("textures/entity/lizard/green.png");
    }

    @Override
    public String getVariantRawId() {
        return this.entityData.get(VARIANT_ID);
    }

    @Override
    public void setVariantRawId(String id) {
        this.entityData.set(VARIANT_ID, id);
    }

    public boolean hasTail() {
        return this.entityData.get(HAS_TAIL);
    }

    public void setHasTail(boolean hasTail) {
        this.entityData.set(HAS_TAIL, hasTail);
    }

    @Override
    public boolean isFollowingOwner() {
        return this.followingOwner;
    }

    @Override
    public void setFollowingOwner(boolean following) {
        this.followingOwner = following;
    }

    @Nullable
    @Override
    public DyeColor getDyeColor() {
        int id = this.entityData.get(DATA_DYE);
        return id < 0 ? null : DyeColor.byId(id);
    }

    @Override
    public void setDyeColor(@Nullable DyeColor color) {
        this.entityData.set(DATA_DYE, color == null ? -1 : color.getId());
    }

    @Override
    public void setTame(boolean tamed, boolean applyTameEffects) {
        super.setTame(tamed, applyTameEffects);
        if (tamed) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20.0);
            this.setHealth(20.0f);
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(8.0);
        }
        Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(4.0);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.saveVariant(compound);
        compound.putBoolean("HasTail", this.hasTail());
        DyeableAnimal.saveDye(this, compound);
        FollowingPet.save(this, compound);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.loadVariant(compound);
        this.setHasTail(compound.getBoolean("HasTail"));
        DyeableAnimal.loadDye(this, compound);
        FollowingPet.load(this, compound);
    }
    //endregion

    //region Spawning
    @Override
    public @NotNull SpawnGroupData finalizeSpawn(ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        this.pickVariantForSpawn(level);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }
    //endregion

    //region Behavior
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LizardTemptGoal(this, 0.6, TEMPT_INGREDIENT, true));
        this.goalSelector.addGoal(6, new PetFollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.hasTail() && this.getHealth() <= this.getMaxHealth() / 2) {
            this.setHasTail(false);
            this.playSound(SoundEvents.SLIME_SQUISH, 1.0f, 1.0f);
            LizardTail lizardTail = NaturalistEntityTypes.LIZARD_TAIL.get().create(this.level());
            if (lizardTail != null) {
                lizardTail.setVariantRawId(this.getVariantRawId());
                lizardTail.setPos(this.getX(), this.getY(), this.getZ());
                this.level().addFreshEntity(lizardTail);
            }
            for (Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(8.0), entity -> entity.getTarget() == this)) {
                mob.setTarget(lizardTail);
            }
            this.tailRegrowCooldown = 12000;
        }
        return super.hurt(source, amount);
    }

    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        InteractionResult whistle = FollowingPet.tryWhistle(this, player, hand);
        if (whistle != null) {
            return whistle;
        }
        Optional<InteractionResult> dyeResult = DyeableAnimal.tryClearDye(this, player, hand);
        if (dyeResult.isEmpty()) {
            dyeResult = DyeableAnimal.tryDye(this, player, hand);
        }
        if (dyeResult.isPresent()) {
            return dyeResult.get();
        }
        if (this.level().isClientSide) {
            boolean bl = this.isOwnedBy(player) || this.isTame() || TEMPT_INGREDIENT.test(stack) && !this.isTame();
            return bl ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (TEMPT_INGREDIENT.test(stack) && this.getHealth() < this.getMaxHealth()) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.heal(5);
                if (!this.hasTail() && this.getHealth() >= this.getMaxHealth()) {
                    this.setHasTail(true);
                    this.playSound(SoundEvents.SLIME_SQUISH, 1.0f, 1.0f);
                }
                return InteractionResult.SUCCESS;
            }
            InteractionResult interactionResult = super.mobInteract(player, hand);
            if (interactionResult.consumesAction() && !this.isBaby() || !this.isOwnedBy(player)) return interactionResult;
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS;
        }
        if (!TEMPT_INGREDIENT.test(stack)) return super.mobInteract(player, hand);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte)7);
            return InteractionResult.SUCCESS;
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.hasTail() && !this.level().isClientSide()) {
            if (this.tailRegrowCooldown > 0) {
                --this.tailRegrowCooldown;
            } else {
                this.playSound(SoundEvents.SLIME_SQUISH, 1.0f, 1.0f);
                this.setHasTail(true);
            }
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.LIZARD_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.LIZARD_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.LIZARD_DEATH.get();
    }

    static class LizardTemptGoal extends TemptGoal {
        @Nullable
        private Player selectedPlayer;
        private final Lizard lizard;

        public LizardTemptGoal(Lizard lizard, double speedModifier, Ingredient ingredient, boolean canScare) {
            super(lizard, speedModifier, ingredient, canScare);
            this.lizard = lizard;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.selectedPlayer == null && this.mob.getRandom().nextInt(this.adjustedTickDelay(600)) == 0) {
                this.selectedPlayer = this.player;
            } else if (this.mob.getRandom().nextInt(this.adjustedTickDelay(500)) == 0) {
                this.selectedPlayer = null;
            }
        }

        @Override
        protected boolean canScare() {
            if (this.selectedPlayer != null && this.selectedPlayer.equals(this.player)) {
                return false;
            }
            return super.canScare();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.lizard.isTame();
        }
    }
    //endregion

    //region Animation
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Lizard> PlayState predicate(final AnimationState<E> event) {
        if (this.isInSittingPose()) {
            event.getController().setAnimation(SIT);
            return PlayState.CONTINUE;
        } else if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            event.getController().setAnimation(WALK);
            event.getController().setAnimationSpeed(2.0D);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }
    //endregion
}
