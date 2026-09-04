from pathlib import Path
import re

ROOT = Path("common/src/main/java/com/crispytwig/naturalist")


def add_import(text: str, qualified_name: str) -> str:
    line = f"import {qualified_name};\n"
    if line in text:
        return text
    imports = list(re.finditer(r"^import .+;\n", text, flags=re.MULTILINE))
    if imports:
        idx = imports[-1].end()
        return text[:idx] + line + text[idx:]
    return text


def migrate_neutral_mob(text: str) -> str:
    if "implements NeutralMob" not in text:
        return text

    text = re.sub(
        r"\n\s*private static final EntityDataAccessor<Integer> REMAINING_ANGER_TIME = SynchedEntityData\.defineId\([^\n]+\);",
        "",
        text,
    )
    text = re.sub(r"\n\s*builder\.define\(REMAINING_ANGER_TIME, 0\);", "", text)

    text = text.replace(
        "    private int remainingPersistentAngerTime;\n    @Nullable\n    private UUID persistentAngerTarget;",
        "    private long persistentAngerEndTime = -1L;\n    @Nullable\n    private EntityReference<LivingEntity> persistentAngerTarget;",
    )
    if "private long persistentAngerEndTime" not in text and "private UUID persistentAngerTarget;" in text:
        text = text.replace(
            "    @Nullable\n    private UUID persistentAngerTarget;",
            "    private long persistentAngerEndTime = -1L;\n    @Nullable\n    private EntityReference<LivingEntity> persistentAngerTarget;",
            1,
        )

    old_remaining = re.compile(
        r"\n\s*@Override\n\s*public void setRemainingPersistentAngerTime\(int time\) \{.*?\n\s*\}\n"
        r"\s*\n\s*@Override\n\s*public int getRemainingPersistentAngerTime\(\) \{.*?\n\s*\}\n",
        flags=re.DOTALL,
    )
    replacement = """
    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }

    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }
"""
    text = old_remaining.sub("\n" + replacement, text, count=1)

    text = text.replace(
        "public void setPersistentAngerTarget(@Nullable UUID target)",
        "public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target)",
    )
    text = text.replace(
        "public UUID getPersistentAngerTarget()",
        "public EntityReference<LivingEntity> getPersistentAngerTarget()",
    )
    text = text.replace(
        "this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));",
        "this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));",
    )
    text = text.replace(
        "this.setPersistentAngerTarget(culprit.getUUID());",
        "this.setPersistentAngerTarget(EntityReference.of(culprit));",
    )

    text = add_import(text, "net.minecraft.world.entity.EntityReference")
    if "UUID" not in text.replace("import java.util.UUID;", ""):
        text = text.replace("import java.util.UUID;\n", "")
    return text


def migrate_elephant(text: str) -> str:
    old_travel = """    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (!this.isAlive()) {
            return;
        }
        LivingEntity livingEntity = this.getControllingPassenger();
        if (!this.isVehicle() || livingEntity == null) {
            super.travel(travelVector);
            return;
        }
        this.setYRot(livingEntity.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(livingEntity.getXRot() * 0.5f);
        this.setRot(this.getYRot(), this.getXRot());
        this.yHeadRot = this.getYRot();
        this.yBodyRot = Mth.rotLerp(0.35F, this.yBodyRot, this.getYRot());
        float f = livingEntity.xxa * 0.5f;
        float g = livingEntity.zza;
        if (this.isControlledByLocalInstance()) {
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.3F);
            super.travel(new Vec3(f, travelVector.y, g));
        } else if (livingEntity instanceof Player) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.calculateEntityAnimation(false);
        this.tryCheckInsideBlocks();
    }
"""
    new_ride = """    @Override
    protected void tickRidden(@NotNull Player controller, @NotNull Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        this.setRot(controller.getYRot(), controller.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(@NotNull Player controller, @NotNull Vec3 selfInput) {
        return new Vec3(controller.xxa * 0.5F, 0.0D, controller.zza);
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player controller) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.3F;
    }
"""
    text = text.replace(old_travel, new_ride)

    old_knockback = """    @Override
    public void knockback(double strength, double x, double z) {
        if (this.isBaby()) {
            double knockbackResistance = this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            super.knockback(strength / Math.max(1.0 - knockbackResistance, 0.01), x, z);
        } else {
            super.knockback(strength, x, z);
        }
    }

"""
    text = text.replace(old_knockback, "")

    old_attack = """    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean shouldHurt = target.hurt(target.damageSources().mobAttack(this), (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (shouldHurt && target instanceof LivingEntity livingEntity) {
            Vec3 knockbackDirection = new Vec3(this.blockPosition().getX() - target.getX(), 0.0, this.blockPosition().getZ() - target.getZ()).normalize();
            float shieldBlockModifier = livingEntity.isDamageSourceBlocked(target.damageSources().mobAttack(this)) ? 0.5f : 1.0f;
            livingEntity.knockback(shieldBlockModifier * 3.0D, knockbackDirection.x(), knockbackDirection.z());
            double knockbackResistance = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(0.0, 0.5f * knockbackResistance, 0.0));
        }
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0f, 1.0f);
        return shouldHurt;
    }
"""
    new_attack = """    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean shouldHurt = super.doHurtTarget(level, target);
        if (shouldHurt && target instanceof LivingEntity livingEntity) {
            Vec3 knockbackDirection = livingEntity.position().subtract(this.position());
            if (knockbackDirection.horizontalDistanceSqr() > 1.0E-6D) {
                knockbackDirection = knockbackDirection.normalize();
                double resistance = Math.max(0.0D, 1.0D - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(
                        knockbackDirection.x * 1.5D * resistance,
                        0.5D * resistance,
                        knockbackDirection.z * 1.5D * resistance));
            }
        }
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
        return shouldHurt;
    }
"""
    text = text.replace(old_attack, new_attack)
    text = text.replace("this.playSound(SoundEvents.HORSE_SADDLE, 1.0F, 1.0F);", "this.playSound(SoundEvents.HORSE_SADDLE.value(), 1.0F, 1.0F);")

    text = text.replace(
        "    protected void dropEquipment() {\n        super.dropEquipment();",
        "    protected void dropEquipment(ServerLevel level) {\n        super.dropEquipment(level);",
    )
    text = text.replace(
        "ItemEntity item = this.spawnAtLocation(stack, this.getBbHeight());",
        "ItemEntity item = this.level() instanceof ServerLevel serverLevel ? this.spawnAtLocation(serverLevel, stack, this.getBbHeight()) : null;",
    )
    text = text.replace(
        "this.mob.doHurtTarget(target);",
        "if (this.mob.level() instanceof ServerLevel serverLevel) {\n                    this.mob.doHurtTarget(serverLevel, target);\n                }",
    )
    return text


def migrate_ostrich(text: str) -> str:
    text = text.replace("import net.minecraft.world.entity.Saddleable;\n", "")
    text = text.replace(", Saddleable, PlayerRideableJumping", ", PlayerRideableJumping")
    text = text.replace("    @Override\n    public boolean isSaddled()", "    public boolean isSaddled()")
    text = text.replace("    @Override\n    public boolean isSaddleable()", "    private boolean canBeSaddled()")
    text = text.replace("this.isSaddleable()", "this.canBeSaddled()")

    old_equip = re.compile(
        r"\n\s*@Override\n\s*public void equipSaddle\(@NotNull ItemStack stack, @Nullable SoundSource source\) \{.*?\n\s*\}\n",
        flags=re.DOTALL,
    )
    text = old_equip.sub("\n", text, count=1)

    old_saddle_interact = """        if (stack.is(Items.SADDLE) && this.canBeSaddled() && !this.isSaddled()) {
            return InteractionResult.PASS;
        }"""
    new_saddle_interact = """        if (stack.is(Items.SADDLE) && this.canBeSaddled() && !this.isSaddled()) {
            if (!this.level().isClientSide()) {
                this.setSaddled(true);
                this.playSound(SoundEvents.HORSE_SADDLE.value(), 0.5F, 1.0F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }"""
    text = text.replace(old_saddle_interact, new_saddle_interact)

    text = text.replace(
        "target.getUUID().equals(this.persistentAngerTarget)",
        "this.persistentAngerTarget != null && this.persistentAngerTarget.matches(target)",
    )
    return text


def migrate_bear(text: str) -> str:
    text = text.replace(
        "AgeableMob baby = (AgeableMob) this.getType().create(serverLevel);",
        "AgeableMob baby = (AgeableMob) this.getType().create(serverLevel, EntitySpawnReason.BREEDING);",
    )
    text = text.replace(
        "new NearestAttackableTargetGoal<>(this, Player.class, 20, true, false, this::isHostileWhenDark)",
        "new NearestAttackableTargetGoal<>(this, Player.class, 20, true, false, (entity, level) -> this.isHostileWhenDark(entity))",
    )
    text = text.replace(
        "new NearestAttackableTargetGoal<>(this, PathfinderMob.class, 10, true, false, (entity) -> entity.getType().is(NaturalistTags.EntityTypes.BEAR_HOSTILES) && !this.isSleeping() && !this.isBaby() && this.canHunt())",
        "new NearestAttackableTargetGoal<>(this, PathfinderMob.class, 10, true, false, (entity, level) -> entity.getType().is(NaturalistTags.EntityTypes.BEAR_HOSTILES) && !this.isSleeping() && !this.isBaby() && this.canHunt())",
    )
    text = text.replace(
        "long dayTime = this.level().getDayTime();\n        return this.wakeTicks <= 0 && (dayTime < 12000 || dayTime > 18000) && dayTime < 23000 && dayTime > 6000 && !this.isAngry() && !this.level().isWaterAt(this.blockPosition());",
        "return this.wakeTicks <= 0 && !this.level().isBrightOutside() && !this.isAngry() && !this.level().isWaterAt(this.blockPosition());",
    )

    old_knockback = """    @Override
    public void knockback(double strength, double x, double z) {
        if (this.isBaby()) {
            super.knockback(strength / Math.max(1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.01), x, z);
        } else {
            super.knockback(strength, x, z);
        }
    }

"""
    text = text.replace(old_knockback, "")
    text = text.replace(
        "public boolean isInvulnerableTo(DamageSource source) {\n        return source.equals(this.damageSources().sweetBerryBush()) || super.isInvulnerableTo(source);",
        "public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {\n        return source.equals(this.damageSources().sweetBerryBush()) || super.isInvulnerableTo(level, source);",
    )
    text = text.replace(
        "public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity killed) {\n        boolean result = super.killedEntity(level, killed);",
        "public boolean killedEntity(@NotNull ServerLevel level, @NotNull LivingEntity killed, @NotNull DamageSource source) {\n        boolean result = super.killedEntity(level, killed, source);",
    )
    return text


def migrate_misc(path: Path, text: str) -> str:
    rel = path.as_posix()
    if rel.endswith("/Lizard.java"):
        text = text.replace(
            "NaturalistEntityTypes.LIZARD_TAIL.get().create(this.level())",
            "NaturalistEntityTypes.LIZARD_TAIL.get().create(this.level(), EntitySpawnReason.TRIGGERED)",
        )
        if "EntitySpawnReason.TRIGGERED" in text:
            text = add_import(text, "net.minecraft.world.entity.EntitySpawnReason")
    elif rel.endswith("/NocturnalHostile.java"):
        text = text.replace(
            "return ((LivingEntity) this).level().isNight();",
            "return !((LivingEntity) this).level().isBrightOutside();",
        )
    return text


def migrate_file(path: Path, text: str) -> str:
    text = migrate_neutral_mob(text)
    if path.name == "Elephant.java":
        text = migrate_elephant(text)
    elif path.name == "Ostrich.java":
        text = migrate_ostrich(text)
    elif path.name == "Bear.java":
        text = migrate_bear(text)
    text = migrate_misc(path, text)
    return text


def main() -> None:
    changed = []
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        migrated = migrate_file(path, original)
        if migrated != original:
            path.write_text(migrated, encoding="utf-8")
            changed.append(str(path))
    print(f"26.2 structural migration changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    import structural_26_2_2
    structural_26_2_2.main()
