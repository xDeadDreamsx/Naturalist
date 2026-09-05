#!/usr/bin/env python3
"""Restore full 1.21.1 TargetingConditions semantics for proximity-driven animal behavior.

Earlier parity waves restored the original radii and explicit player filters after Level's
getNearbyPlayers(TargetingConditions, ...) API changed. Those first adaptations still used plain
getEntitiesOfClass predicates, which dropped the remaining forNonCombat targeting rules. Reapply
the original TargetingConditions through the 26.2 ServerLevel predicate API.
"""

from pathlib import Path
import runpy

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob")


def replace_method(path: Path, marker: str, replacement: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if replacement in text:
        return False
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f"Could not locate {marker!r} in {path}")
    brace = text.find("{", start)
    if brace < 0:
        raise RuntimeError(f"Could not locate body for {marker!r} in {path}")
    depth = 0
    i = brace
    in_string = in_char = escaped = line_comment = block_comment = False
    while i < len(text):
        c = text[i]
        n = text[i + 1] if i + 1 < len(text) else ""
        if line_comment:
            if c == "\n":
                line_comment = False
        elif block_comment:
            if c == "*" and n == "/":
                block_comment = False
                i += 1
        elif in_string:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif c == '"':
                in_string = False
        elif in_char:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif c == "'":
                in_char = False
        else:
            if c == "/" and n == "/":
                line_comment = True
                i += 1
            elif c == "/" and n == "*":
                block_comment = True
                i += 1
            elif c == '"':
                in_string = True
            elif c == "'":
                in_char = True
            elif c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 0:
                    path.write_text(text[:start] + replacement + text[i + 1:], encoding="utf-8")
                    return True
        i += 1
    raise RuntimeError(f"Unterminated method {marker!r} in {path}")


def main() -> None:
    changed: list[str] = []

    replacements = [
        ("Snail.java", "    public boolean canHide() {", """    public boolean canHide() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(5.0D)
                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));
        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),
                player -> conditions.test(serverLevel, this, player)).isEmpty();
    }
"""),
        ("Tortoise.java", "    public boolean canHide() {", """    public boolean canHide() {
        if (this.isTame() || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(5.0D)
                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
                        && !entity.isDiscrete() && !entity.isHolding(temptItems()));
        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),
                player -> conditions.test(serverLevel, this, player)).isEmpty();
    }
"""),
        ("Crab.java", "    private boolean thinkCanHide() {", """    private boolean thinkCanHide() {
        if (this.isBaby() || this.isTame() || !this.getMainHandItem().isEmpty()
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(4.0D)
                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),
                player -> conditions.test(serverLevel, this, player));
        boolean playerNear = false;
        for (Player player : players) {
            if (!player.isCrouching() && !foodItems().test(player.getMainHandItem()) && !foodItems().test(player.getOffhandItem())) {
                playerNear = true;
                break;
            }
        }
        return playerNear && this.findNearbyWeapon() == null;
    }
"""),
        ("Hedgehog.java", "    private boolean thinkCanHide() {", """    private boolean thinkCanHide() {
        if (this.isTame() || this.isRolling() || this.isSprinting() || this.isInSittingPose()
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(6.0D)
                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                player -> conditions.test(serverLevel, this, player));
        for (Player player : players) {
            if (!player.isCrouching() && !foodItems().test(player.getMainHandItem()) && !foodItems().test(player.getOffhandItem())) {
                return true;
            }
        }
        return false;
    }
"""),
        ("Ostrich.java", "    private boolean thinkCanHide() {", """    private boolean thinkCanHide() {
        if (this.isTame() || this.isBaby() || this.isAggressive() || this.isVehicle()
                || !(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(16.0D)
                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)
                        && !entity.isDiscrete() && !entity.isHolding(foodItems()));
        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                player -> conditions.test(serverLevel, this, player)).isEmpty();
    }
"""),
        ("GiantIsopod.java", "    private boolean computeCanHide() {", """    private boolean computeCanHide() {
        if (this.isBaby()) {
            return false;
        }
        if (this.hideHoldTicks > 0) {
            return true;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(3.0D)
                .selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));
        return !serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),
                player -> conditions.test(serverLevel, this, player)).isEmpty();
    }
"""),
        ("Snake.java", "    private boolean canRattle() {", """    private boolean canRattle() {
        boolean rattlesnake = this.isRattlesnake();
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        TargetingConditions conditions = TargetingConditions.forNonCombat().range(4.0D);
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),
                player -> conditions.test(serverLevel, this, player));
        if (!players.isEmpty() && rattlesnake && !players.getFirst().isCreative()) {
            this.setTarget(players.getFirst());
        } else {
            this.setTarget(null);
        }
        return !players.isEmpty() && rattlesnake;
    }
"""),
        ("Vulture.java", "        public boolean canUse() {", """        public boolean canUse() {
            if (this.vulture.getTarget() != null) {
                return false;
            }
            if (this.vulture.level() instanceof ServerLevel serverLevel) {
                this.toAvoid = serverLevel.getNearestPlayer(this.vulture.getX(), this.vulture.getY(), this.vulture.getZ(), this.detectRange,
                        entity -> entity instanceof Player player && this.fleeConditions.test(serverLevel, this.vulture, player));
            } else {
                this.toAvoid = null;
            }
            return this.toAvoid != null;
        }
"""),
    ]

    for filename, marker, replacement in replacements:
        path = ROOT / filename
        if filename == "Vulture.java":
            text = path.read_text(encoding="utf-8")
            class_pos = text.find("static class VultureFleePlayerGoal")
            if class_pos < 0:
                raise RuntimeError("Could not locate VultureFleePlayerGoal")
            prefix = text[:class_pos]
            suffix_path = Path(str(path) + ".tmp_parity")
            suffix_path.write_text(text[class_pos:], encoding="utf-8")
            try:
                did = replace_method(suffix_path, marker, replacement)
                if did:
                    path.write_text(prefix + suffix_path.read_text(encoding="utf-8"), encoding="utf-8")
                    changed.append(str(path))
            finally:
                if suffix_path.exists():
                    suffix_path.unlink()
        elif replace_method(path, marker, replacement):
            changed.append(str(path))

    print(f"26.2 full targeting-conditions parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_21.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
