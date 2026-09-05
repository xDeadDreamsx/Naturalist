#!/usr/bin/env python3
"""Restore Bird behavior that was weakened or dropped by the Minecraft 26.2 migration.

Naturalist 1.21.1 deliberately makes Bird permanently adult and uses a complete
TargetingConditions.forCombat() predicate when deciding which nearby player to flee from.
The 26.2 port lost the isBaby override and replaced the targeting conditions with a simpler
radius/predicate query. Restore both original semantics using the 26.2 ServerLevel query API.
"""

from pathlib import Path
import runpy

BIRD = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Bird.java")


def main() -> None:
    text = BIRD.read_text(encoding="utf-8")
    original = text

    # Naturalist birds never have a baby state in 1.21.1. The override disappeared during the
    # AgeableMob API migration, which can otherwise expose age-dependent behavior through NBT or
    # commands even though birds cannot breed.
    baby_method = """    @Override
    public boolean isBaby() {
        return false;
    }
"""
    if baby_method not in text:
        anchor = """    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Bird breed-offspring insertion point")
        text = text.replace(anchor, anchor + "\n" + baby_method, 1)

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
