#!/usr/bin/env python3
"""Restore Bird behavior that was weakened or dropped by the Minecraft 26.2 migration.

Naturalist 1.21.1 deliberately makes Bird permanently adult and uses a complete
TargetingConditions.forCombat() predicate when deciding which nearby player to flee from.
Minecraft 26.2 made AgeableMob#isBaby final and added canBeABaby() as the supported hook,
so restore the old semantics through that hook instead of overriding isBaby directly.
"""

from pathlib import Path
import re
import runpy

BIRD = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Bird.java")


def main() -> None:
    text = BIRD.read_text(encoding="utf-8")
    original = text

    # Older migration attempts inserted the old isBaby() override. Wave 4 strips the method on
    # 26.2 because AgeableMob#isBaby is final, but could leave its @Override annotation behind.
    # Clean both forms before adding the supported 26.2 capability hook.
    text = re.sub(
        r"\n\s*@Override\s*\n\s*public boolean isBaby\(\) \{\s*return false;\s*\}\s*\n",
        "\n",
        text,
        count=1,
    )
    text = re.sub(
        r"(?:\n\s*@Override\s*)+\n\s*//endregion",
        "\n    //endregion",
        text,
        count=1,
    )

    adult_hook = """    @Override
    protected boolean canBeABaby() {
        return false;
    }
"""
    if adult_hook not in text:
        anchor = """    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Bird breed-offspring insertion point")
        text = text.replace(anchor, anchor + "\n" + adult_hook, 1)

    # Keep the complete forCombat targeting semantics. The simplified coordinate query used by
    # the first 26.2 port only kept creative/spectator and crouching filters and could therefore
    # make birds flee from players the original TargetingConditions rejected.
    old = """            this.toAvoid = this.bird.level().getNearestPlayer(this.bird.getX(), this.bird.getY(), this.bird.getZ(), MAX_DIST, entity -> entity instanceof Player player && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && !player.isDiscrete());
            if (this.toAvoid == null) {
                return false;
            }
"""
    new = """            if (this.bird.level() instanceof ServerLevel serverLevel) {
                this.toAvoid = serverLevel.getNearestPlayer(this.bird.getX(), this.bird.getY(), this.bird.getZ(), MAX_DIST,
                        entity -> entity instanceof Player player && this.avoidTargeting.test(serverLevel, this.bird, player));
            } else {
                this.toAvoid = null;
            }
            if (this.toAvoid == null) {
                return false;
            }
"""
    if new not in text:
        if old not in text:
            raise RuntimeError("Could not locate BirdAvoidPlayerGoal player lookup")
        text = text.replace(old, new, 1)

    if text != original:
        BIRD.write_text(text, encoding="utf-8")
        print(f"26.2 Bird behavior parity pass changed {BIRD}")
    else:
        print("26.2 Bird behavior parity pass changed 0 files")


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_19.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
