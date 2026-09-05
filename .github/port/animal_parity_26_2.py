from pathlib import Path


def replace(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        return False
    path.write_text(text.replace(old, new), encoding="utf-8")
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

print(f"26.2 animal parity pass changed {len(changed)} files")
for path in changed:
    print(path)
