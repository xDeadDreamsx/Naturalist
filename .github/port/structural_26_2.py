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
    if path.name == "Ostrich.java":
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
