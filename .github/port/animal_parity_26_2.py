from pathlib import Path


def replace(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        return False
    path.write_text(text.replace(old, new), encoding="utf-8")
    return True


changed = []

# Hiding/alert states are used by client animation code as well as server AI. During the 26.2
# targeting API migration several of these predicates became ServerLevel-only, making the server
# behave correctly while the client permanently saw the non-hidden/non-alert pose.
crab = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Crab.java")
crab_old = '''    private boolean thinkCanHide() {\n        if (this.isBaby() || this.isTame() || !this.getMainHandItem().isEmpty()\n                || !(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(4.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));\n        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),\n                player -> conditions.test(serverLevel, this, player));\n'''
crab_new = '''    private boolean thinkCanHide() {\n        if (this.isBaby() || this.isTame() || !this.getMainHandItem().isEmpty()) {\n            return false;\n        }\n        List<Player> players = this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && this.distanceToSqr(player) <= 16.0D);\n'''
if replace(crab, crab_old, crab_new):
    changed.append(str(crab))

isopod = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/GiantIsopod.java")
isopod_old = '''        if (!(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(3.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));\n        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),\n                player -> conditions.test(serverLevel, this, player)).isEmpty();\n'''
isopod_new = '''        return !this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && this.distanceToSqr(player) <= 9.0D).isEmpty();\n'''
if replace(isopod, isopod_old, isopod_new):
    changed.append(str(isopod))

hedgehog = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Hedgehog.java")
hedgehog_old = '''    private boolean thinkCanHide() {\n        if (this.isTame() || this.isRolling() || this.isSprinting() || this.isInSittingPose()\n                || !(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(6.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));\n        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),\n                player -> conditions.test(serverLevel, this, player));\n'''
hedgehog_new = '''    private boolean thinkCanHide() {\n        if (this.isTame() || this.isRolling() || this.isSprinting() || this.isInSittingPose()) {\n            return false;\n        }\n        List<Player> players = this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && this.distanceToSqr(player) <= 36.0D);\n'''
if replace(hedgehog, hedgehog_old, hedgehog_new):
    changed.append(str(hedgehog))

snail = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Snail.java")
snail_old = '''    public boolean canHide() {\n        if (!(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(5.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));\n        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),\n                player -> conditions.test(serverLevel, this, player)).isEmpty();\n    }'''
snail_new = '''    public boolean canHide() {\n        return !this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && this.distanceToSqr(player) <= 25.0D).isEmpty();\n    }'''
if replace(snail, snail_old, snail_new):
    changed.append(str(snail))

snake = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Snake.java")
snake_old = '''    private boolean canRattle() {\n        boolean rattlesnake = this.isRattlesnake();\n        if (!(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(4.0D);\n        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),\n                player -> conditions.test(serverLevel, this, player));\n        if (!players.isEmpty() && rattlesnake && !players.getFirst().isCreative()) {\n            this.setTarget(players.getFirst());\n        } else {\n            this.setTarget(null);\n        }\n        return !players.isEmpty() && rattlesnake;\n    }'''
snake_new = '''    private boolean canRattle() {\n        boolean rattlesnake = this.isRattlesnake();\n        List<Player> players = this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),\n                player -> player.isAlive() && !player.isSpectator() && this.distanceToSqr(player) <= 16.0D);\n        if (!this.level().isClientSide()) {\n            if (!players.isEmpty() && rattlesnake && !players.getFirst().isCreative()) {\n                this.setTarget(players.getFirst());\n            } else {\n                this.setTarget(null);\n            }\n        }\n        return !players.isEmpty() && rattlesnake;\n    }'''
if replace(snake, snake_old, snake_new):
    changed.append(str(snake))

tortoise = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Tortoise.java")
tortoise_old = '''    public boolean canHide() {\n        if (this.isTame() || !(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(5.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)\n                        && !entity.isDiscrete() && !entity.isHolding(temptItems()));\n        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),\n                player -> conditions.test(serverLevel, this, player)).isEmpty();\n    }'''
tortoise_new = '''    public boolean canHide() {\n        if (this.isTame()) {\n            return false;\n        }\n        return !this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && !player.isDiscrete()\n                        && !player.isHolding(temptItems())\n                        && this.distanceToSqr(player) <= 25.0D).isEmpty();\n    }'''
if replace(tortoise, tortoise_old, tortoise_new):
    changed.append(str(tortoise))

ostrich = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Ostrich.java")
ostrich_old = '''    private boolean thinkCanHide() {\n        if (this.isTame() || this.isBaby() || this.isAggressive() || this.isVehicle()\n                || !(this.level() instanceof ServerLevel serverLevel)) {\n            return false;\n        }\n        TargetingConditions conditions = TargetingConditions.forNonCombat().range(16.0D)\n                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)\n                        && !entity.isDiscrete() && !entity.isHolding(foodItems()));\n        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),\n                player -> conditions.test(serverLevel, this, player)).isEmpty();\n    }'''
ostrich_new = '''    private boolean thinkCanHide() {\n        if (this.isTame() || this.isBaby() || this.isAggressive() || this.isVehicle()) {\n            return false;\n        }\n        return !this.level().getEntitiesOfClass(Player.class,\n                this.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),\n                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)\n                        && !player.isDiscrete()\n                        && !player.isHolding(foodItems())\n                        && this.distanceToSqr(player) <= 256.0D).isEmpty();\n    }'''
if replace(ostrich, ostrich_old, ostrich_new):
    changed.append(str(ostrich))

# Remove targeting imports only when the class no longer references the type after the patches.
for mob_path in (crab, isopod, hedgehog, snail, snake, tortoise, ostrich):
    text = mob_path.read_text(encoding="utf-8")
    if "TargetingConditions" not in text.replace("import net.minecraft.world.entity.ai.targeting.TargetingConditions;", ""):
        text = text.replace("import net.minecraft.world.entity.ai.targeting.TargetingConditions;\n", "")
        mob_path.write_text(text, encoding="utf-8")

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
