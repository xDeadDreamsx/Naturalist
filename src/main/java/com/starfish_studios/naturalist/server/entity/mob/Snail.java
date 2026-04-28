package com.starfish_studios.naturalist.server.entity.mob;

import com.starfish_studios.naturalist.server.entity.base.*;
import com.starfish_studios.naturalist.server.entity.ai.goal.EggLayingBreedGoal;
import com.starfish_studios.naturalist.server.entity.ai.goal.LayEggGoal;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.registry.NaturalistRegistry;
import com.starfish_studios.naturalist.registry.NaturalistSoundEvents;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.animation.AnimationState;

import java.util.*;

@SuppressWarnings("unused")
public class Snail extends ClimbingAnimal implements NaturalistGeoEntity, Bucketable, HidingAnimal, EggLayingAnimal {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.BEETROOT);
    private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(Snail.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(Snail.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Snail.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Snail.class, EntityDataSerializers.BOOLEAN);
    int layEggCounter;

    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.snail.idle");
    protected static final RawAnimation CRAWL = RawAnimation.begin().thenLoop("animation.sf_nba.snail.crawl");
    protected static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("animation.sf_nba.snail.climb");
    protected static final RawAnimation HIDE = RawAnimation.begin().thenPlay("animation.sf_nba.snail.hide_start").thenLoop("animation.sf_nba.snail.hide_idle");

    public Snail(@NotNull EntityType<? extends NaturalistAnimal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 2.0D).add(Attributes.MOVEMENT_SPEED, 0.1F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new EggLayingBreedGoal<>(this, 1.0));
        this.goalSelector.addGoal(1, new LayEggGoal<>(this, 1.0));
        this.goalSelector.addGoal(2, new SnailStrollGoal(this, 0.9D, 0.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(this.canHide() ? strength / 4 : strength, x, z);
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        return super.hurt(source, this.canHide() ? amount * 0.8F : amount);
    }

    @Override
    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    @Override
    public void setHasEgg(boolean hasEgg) {
        this.entityData.set(HAS_EGG, hasEgg);
    }

    @Override
    public Block getEggBlock() {
        return NaturalistRegistry.SNAIL_EGGS.get();
    }

    @Override
    public TagKey<Block> getEggLayableBlockTag() {
        return NaturalistTags.BlockTags.ALLIGATOR_EGG_LAYABLE_ON;
    }

    @Override
    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    @Override
    public void setLayingEgg(boolean isLayingEgg) {
        this.layEggCounter = isLayingEgg ? 1 : 0;
        this.entityData.set(LAYING_EGG, isLayingEgg);
    }

    @Override
    public int getLayEggCounter() {
        return this.layEggCounter;
    }

    @Override
    public void setLayEggCounter(int layEggCounter) {
        this.layEggCounter = layEggCounter;
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.hasEgg();
    }

    @Override
    protected float getClimbSpeedMultiplier() {
        return 0.5F;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.SNAIL.get().create(serverLevel);
    }

    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    public void travel(@NotNull Vec3 vec3) {
        super.travel(vec3);
        if (this.canHide()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0, 1, 0));
            vec3 = vec3.multiply(0, 1, 0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.canHide() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FROM_BUCKET, false);
        builder.define(DATA_COLOR, Color.BROWN.getId());
        builder.define(HAS_EGG, false);
        builder.define(LAYING_EGG, false);
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
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("FromBucket", this.fromBucket());
        compound.putByte("Color", (byte)this.getSnailColor().getId());
        compound.putBoolean("HasEgg", this.hasEgg());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setFromBucket(compound.getBoolean("FromBucket"));
        this.setSnailColor(Color.BY_ID[compound.getInt("Color")]);
        this.setHasEgg(compound.getBoolean("HasEgg"));
    }

    public Color getSnailColor() {
        return Snail.Color.BY_ID[this.entityData.get(DATA_COLOR)];
    }

    public void setSnailColor(Snail.Color color) {
        this.entityData.set(DATA_COLOR, color.getId());
    }

    public DyeColor getColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLOR));
    }

    public void setColor(@NotNull DyeColor color) {
        this.entityData.set(DATA_COLOR, color.getId());
    }

    public enum Color {
        WHITE(0, "white"),
        ORANGE(1, "orange"),
        MAGENTA(2, "magenta"),
        LIGHT_BLUE(3, "light_blue"),
        YELLOW(4, "yellow"),
        LIME(5, "lime"),
        PINK(6, "pink"),
        GRAY(7, "gray"),
        LIGHT_GRAY(8, "light_gray"),
        CYAN(9, "cyan"),
        PURPLE(10, "purple"),
        BLUE(11, "blue"),
        BROWN(12, "brown"),
        GREEN(13, "green"),
        RED(14, "red"),
        BLACK(15, "black");

        public static final Snail.Color[] BY_ID = Arrays.stream(values()).sorted(Comparator.comparingInt(Snail.Color::getId)).toArray(Snail.Color[]::new);
        private final int id;
        private final String name;

        Color(int j, String string2) {
            this.id = j;
            this.name = string2;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static Snail.Color getTypeById(int id) {
            for (Snail.Color type : values()) {
                if (type.id == id) return type;
            }
            return Snail.Color.BROWN;
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();
        label90: {
                if (!(item instanceof DyeItem dyeItem)) {
                    break label90;
                }

            DyeColor dyeColor = dyeItem.getDyeColor();
                if (dyeColor != this.getColor()) {
                    this.setColor(dyeColor);
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                    return InteractionResult.SUCCESS;
                }
            }
        return bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    static <T extends LivingEntity & Bucketable> Optional<InteractionResult> bucketMobPickup(@NotNull Player player, @NotNull InteractionHand hand, T entity) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.BUCKET && entity.isAlive()) {
            entity.playSound(entity.getPickupSound(), 1.0F, 1.0F);
            ItemStack bucketStack = entity.getBucketItemStack();
            entity.saveToBucketTag(bucketStack);
            ItemStack resultStack = ItemUtils.createFilledResult(stack, player, bucketStack, false);
            player.setItemInHand(hand, resultStack);
            Level level = entity.level();
            if (!level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer)player, bucketStack);
            }

            entity.discard();
            return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void saveToBucketTag(@NotNull ItemStack stack) {
        stack.set(DataComponents.CUSTOM_NAME, this.getCustomName());
        CustomData.update(DataComponents.BUCKET_ENTITY_DATA, stack, tag -> {
            if (this.isNoAi()) tag.putBoolean("NoAI", true);
            if (this.isSilent()) tag.putBoolean("Silent", true);
            if (this.isNoGravity()) tag.putBoolean("NoGravity", true);
            if (this.hasGlowingTag()) tag.putBoolean("Glowing", true);
            if (this.isInvulnerable()) tag.putBoolean("Invulnerable", true);
            tag.putFloat("Health", this.getHealth());
        });
        CompoundTag compoundTag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        compoundTag.putInt("Color", this.getSnailColor().getId());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
    }

    @Override
    public void loadFromBucketTag(@NotNull CompoundTag tag) {
        if (tag.contains("NoAI")) this.setNoAi(tag.getBoolean("NoAI"));
        if (tag.contains("Silent")) this.setSilent(tag.getBoolean("Silent"));
        if (tag.contains("NoGravity")) this.setNoGravity(tag.getBoolean("NoGravity"));
        if (tag.contains("Glowing")) this.setGlowingTag(tag.getBoolean("Glowing"));
        if (tag.contains("Invulnerable")) this.setInvulnerable(tag.getBoolean("Invulnerable"));
        if (tag.contains("Health", 99)) this.setHealth(tag.getFloat("Health"));

        if (tag.contains("Color", 3)) {
            int i = tag.getInt("Color");
            if (i >= 0 && i < Snail.Color.BY_ID.length) {
                this.setSnailColor(Snail.Color.BY_ID[i]);
            }
        } else {
            this.setSnailColor(Snail.Color.BROWN);
        }
    }

    @Override
    public @NotNull ItemStack getBucketItemStack() {
        return new ItemStack(NaturalistRegistry.SNAIL_BUCKET.get());
    }

    @Override
    public @NotNull SoundEvent getPickupSound() {
        return NaturalistSoundEvents.BUCKET_FILL_SNAIL.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.SNAIL_AMBIENT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return NaturalistSoundEvents.SNAIL_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.SNAIL_CRUSH.get();
    }

    @Override
    public boolean canHide() {
        List<Player> players = this.level().getNearbyPlayers(TargetingConditions.forNonCombat().range(5.0D).selector(EntitySelector.NO_CREATIVE_OR_SPECTATOR::test), this, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));
        return !players.isEmpty();
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Snail> PlayState predicate(final @NotNull AnimationState<E> event) {
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6) {
            event.getController().setAnimation(CRAWL);
        } else if (this.isNaturalistClimbing()){
            event.getController().setAnimation(CLIMB);
        } else {
            event.getController().setAnimation(IDLE);
        }
        return PlayState.CONTINUE;
    }

    private <E extends Snail> PlayState hidePredicate(final AnimationState<E> event) {
        if( this.canHide()) {
            event.getController().setAnimation(HIDE);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();

        return PlayState.STOP;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Snail> event) {
        Snail snail = event.getAnimatable();
        if (snail.level().isClientSide) {
            if (event.getKeyframeData().getSound().equals("forward")) {
                snail.level().playLocalSound(snail.getX(), snail.getY(), snail.getZ(), NaturalistSoundEvents.SNAIL_FORWARD.get(), snail.getSoundSource(), 0.5F, 1.0F, false);
            }
            if (event.getKeyframeData().getSound().equals("back")) {
                snail.level().playLocalSound(snail.getX(), snail.getY(), snail.getZ(), NaturalistSoundEvents.SNAIL_BACK.get(), snail.getSoundSource(), 0.5F, 1.0F, false);
            }
            if (event.getKeyframeData().getSound().equals("hide")) {
                snail.level().playLocalSound(snail.getX(), snail.getY(), snail.getZ(), NaturalistSoundEvents.TORTOISE_HIDE.get(), snail.getSoundSource(), 0.2F, 1.7F, false);
            }
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.@NotNull ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate).setSoundKeyframeHandler(this::soundListener));
        controllers.add(new AnimationController<>(this, "hideController", 0, this::hidePredicate).setSoundKeyframeHandler(this::soundListener));
    }

    static class SnailStrollGoal extends WaterAvoidingRandomStrollGoal {
        public SnailStrollGoal(PathfinderMob mob, double speedModifier, float probability) {
            super(mob, speedModifier, probability);
            this.forceTrigger = true;
            this.interval = 1;
        }
    }

}
