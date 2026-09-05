#!/usr/bin/env python3
"""Restore Snake behavior accidentally dropped by earlier structural migration waves.

The 1.21.1 Snake defines its own spawn rule/equipment, goal selectors, pet targeting, movement
penalty while carrying prey, poison attack, carried-item drop on hurt, and custom persistence.
Those methods were lost while adapting the class to 26.2 signatures. Re-add the original logic
using the 26.2 ValueInput/ValueOutput, EntitySpawnReason, ServerLevel combat, and two-argument
target predicate APIs.
"""

from pathlib import Path

SNAKE = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Snake.java")
NATURALIST = Path("common/src/main/java/com/crispytwig/naturalist/Naturalist.java")


def main() -> None:
    changed = []
    text = SNAKE.read_text(encoding="utf-8")

    if "protected void registerGoals()" not in text:
        anchor = """    @Override
    public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity killed, @NotNull DamageSource source) {
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Snake behavior insertion point")

        restored = """    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        this.saveVariant(output);
        this.addPersistentAngerSaveData(output);
        FollowingPet.savePet(this, output);
        this.saveHuntingCooldown(output);
    }

    @Override
    public void readAdditionalSaveData(@NotNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.loadVariant(input);
        this.readPersistentAngerSaveData(this.level(), input);
        FollowingPet.loadPet(this, input);
        this.loadHuntingCooldown(input);
    }

    public static boolean checkSnakeSpawnRules(EntityType<Snake> entityType, LevelAccessor level, EntitySpawnReason type, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.DIRT) && isBrightEnoughToSpawn(level, pos);
    }

    @Override
    public @NotNull SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull EntitySpawnReason reason, @Nullable SpawnGroupData spawnData) {
        this.selectVariantForSpawn(level);
        this.populateDefaultEquipmentSlots(random, difficulty);
        return super.finalizeSpawn(level, difficulty, reason, spawnData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource random, @NotNull DifficultyInstance difficulty) {
        if (random.nextFloat() < 0.2F) {
            float chance = random.nextFloat();
            ItemStack stack;
            if (chance < 0.05F) {
                stack = new ItemStack(Items.RABBIT_FOOT);
            } else if (chance < 0.1F) {
                stack = new ItemStack(Items.SLIME_BALL);
            } else if (chance < 0.15F) {
                stack = new ItemStack(Items.FEATHER);
            } else if (chance < 0.3F) {
                stack = new ItemStack(Items.RABBIT);
            } else {
                stack = new ItemStack(Items.CHICKEN);
            }
            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(1, new SnakeMeleeAttackGoal(this, 1.75D, true));
        this.goalSelector.addGoal(2, new SearchForItemsGoal(this, 1.2F, foodItems(), 8.0D, 8.0D));
        this.goalSelector.addGoal(3, new SleepGoal<>(this));
        this.goalSelector.addGoal(4, new PetFollowOwnerGoal(this, 1.2D, 10.0F, 2.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                (player, level) -> this.isAngryAt(player, level)));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false,
                (livingEntity, level) -> this.canHunt() && (livingEntity.getType().builtInRegistryHolder().is(NaturalistTags.EntityTypes.SNAKE_HOSTILES)
                        || (livingEntity instanceof Slime slime && slime.isTiny()))));
        this.targetSelector.addGoal(6, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Override
    public boolean wantsToAttack(@NotNull LivingEntity target, @NotNull LivingEntity owner) {
        return PetTargeting.wantsToAttack(target, owner);
    }

    @Override
    protected float getClimbSpeedMultiplier() {
        return 0.5F;
    }

    @Override
    public float getSpeed() {
        return this.getMainHandItem().isEmpty() ? super.getSpeed() : super.getSpeed() * 0.5F;
    }

    @Override
    public boolean hurtServer(@NotNull ServerLevel level, @NotNull DamageSource source, float amount) {
        if (!this.getMainHandItem().isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(level, this.getX() + this.getLookAngle().x, this.getY() + 1.0D,
                    this.getZ() + this.getLookAngle().z, this.getMainHandItem());
            itemEntity.setPickUpDelay(80);
            itemEntity.setThrower(this);
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            level.addFreshEntity(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    public boolean doHurtTarget(@NotNull ServerLevel level, @NotNull Entity entity) {
        if (this.isVenomous() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 40), this);
        }
        return super.doHurtTarget(level, entity);
    }

    @Override
    public boolean canAttack(@NotNull LivingEntity target) {
        return PetTargeting.protectsOwnedPet(this, target) && super.canAttack(target);
    }

"""
        text = text.replace(anchor, restored + anchor, 1)
        SNAKE.write_text(text, encoding="utf-8")
        changed.append(str(SNAKE))

    ntext = NATURALIST.read_text(encoding="utf-8")
    generic = "r.register(NaturalistEntityTypes.SNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Animal::checkAnimalSpawnRules);"
    exact = "r.register(NaturalistEntityTypes.SNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules);"
    if generic in ntext:
        NATURALIST.write_text(ntext.replace(generic, exact, 1), encoding="utf-8")
        changed.append(str(NATURALIST))
    elif exact not in ntext:
        raise RuntimeError("Could not restore Naturalist Snake spawn placement predicate")

    print(f"26.2 Snake behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
