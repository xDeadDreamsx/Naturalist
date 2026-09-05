from pathlib import Path


def replace(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        return False
    path.write_text(text.replace(old, new), encoding="utf-8")
    return True


def ensure_import(path: Path, import_line: str, after: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if import_line in text:
        return False
    if after not in text:
        return False
    path.write_text(text.replace(after, after + import_line), encoding="utf-8")
    return True


changed = []

crab = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Crab.java")
crab_old = '''    private boolean thinkCanHide() {\n        if (this.isBaby() || this.isTame() || !this.getMainHandItem().isEmpty()\n                || !(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(4.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));\n        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),\n                player -> conditions.test(serverLevel, this, player));\n'''
crab_new = '''    private boolean thinkCanHide() {\n        if (this.isBaby() || this.isTame() || !this.getMainHandItem().isEmpty()) {\n            return false;\n        }\n        List<Player> players = this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && this.distanceToSqr(player) <= 16.0D);\n'''
if replace(crab, crab_old, crab_new):
    text = crab.read_text(encoding="utf-8")
    text = text.replace("import net.minecraft.world.entity.ai.targeting.TargetingConditions;\n", "")
    crab.write_text(text, encoding="utf-8")
    changed.append(str(crab))

# Older migration waves temporarily made Giant Isopod hiding server-only. Keep the fix
# idempotently here so future migration replays cannot regress the client hide animation.
isopod = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/GiantIsopod.java")
isopod_old = '''        if (!(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(3.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));\n        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),\n                player -> conditions.test(serverLevel, this, player)).isEmpty();\n'''
isopod_new = '''        return !this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && this.distanceToSqr(player) <= 9.0D).isEmpty();\n'''
if replace(isopod, isopod_old, isopod_new):
    text = isopod.read_text(encoding="utf-8")
    text = text.replace("import net.minecraft.world.entity.ai.targeting.TargetingConditions;\n", "")
    isopod.write_text(text, encoding="utf-8")
    changed.append(str(isopod))

# 1.21.1 used Level#getTimeOfDay to give alligator and tortoise eggs a preferred hatch
# window. A temporary 26.2 approximation reduced time to two values (0.25/0.75), making the
# original 0.65-0.69 window impossible. 26.2 exposes the same continuous sun cycle through
# EnvironmentAttributes.SUN_ANGLE, measured in degrees.
for egg_name in ("AlligatorEggBlock.java", "TortoiseEggBlock.java"):
    egg = Path("common/src/main/java/com/crispytwig/naturalist/server/block") / egg_name
    egg_changed = False
    text = egg.read_text(encoding="utf-8")

    if "this.shouldUpdateHatchLevel(level)" in text:
        text = text.replace("this.shouldUpdateHatchLevel(level)", "this.shouldUpdateHatchLevel(level, pos)")
        egg_changed = True

    bad_method = '''    private boolean shouldUpdateHatchLevel(Level level) {\n        float timeOfDay = level.isBrightOutside() ? 0.25F : 0.75F;\n        return timeOfDay < 0.69F && timeOfDay > 0.65F || level.getRandom().nextInt(500) == 0;\n    }'''
    bad_method_notnull = '''    private boolean shouldUpdateHatchLevel(@NotNull Level level) {\n        float timeOfDay = level.isBrightOutside() ? 0.25F : 0.75F;\n        return timeOfDay < 0.69F && timeOfDay > 0.65F || level.getRandom().nextInt(500) == 0;\n    }'''
    replacement_plain = '''    private boolean shouldUpdateHatchLevel(Level level, BlockPos pos) {\n        float timeOfDay = level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, pos) / 360.0F;\n        return timeOfDay < 0.69F && timeOfDay > 0.65F || level.getRandom().nextInt(500) == 0;\n    }'''
    replacement_notnull = '''    private boolean shouldUpdateHatchLevel(@NotNull Level level, @NotNull BlockPos pos) {\n        float timeOfDay = level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, pos) / 360.0F;\n        return timeOfDay < 0.69F && timeOfDay > 0.65F || level.getRandom().nextInt(500) == 0;\n    }'''

    if bad_method in text:
        text = text.replace(bad_method, replacement_plain)
        egg_changed = True
    elif bad_method_notnull in text:
        text = text.replace(bad_method_notnull, replacement_notnull)
        egg_changed = True

    if "EnvironmentAttributes.SUN_ANGLE" in text and "import net.minecraft.world.attribute.EnvironmentAttributes;" not in text:
        anchor = "import net.minecraft.util.RandomSource;\n"
        if anchor in text:
            text = text.replace(anchor, anchor + "import net.minecraft.world.attribute.EnvironmentAttributes;\n")
            egg_changed = True

    if egg_changed:
        egg.write_text(text, encoding="utf-8")
        changed.append(str(egg))

print(f"26.2 animal parity pass changed {len(changed)} files")
for path in changed:
    print(path)
