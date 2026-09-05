#!/usr/bin/env python3
"""Restore Naturalist 1.21.1 behavior semantics that were broadened during the 26.2 API port.

The API migration had replaced two original time predicates with Level#isBrightOutside().
That is not behaviorally equivalent: Lion uses a very specific bask/sleep window, while
KomodoDragon used Level#isDay(). Keep the original Naturalist predicates and only adapt
method signatures where Minecraft 26.2 requires it.
"""

from pathlib import Path

LION = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Lion.java")
KOMODO = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/KomodoDragon.java")


def replace_once(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if new in text:
        return False
    if old not in text:
        raise RuntimeError(f"Expected 26.2 migration pattern not found in {path}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")
    return True


def main() -> None:
    changed = []

    lion_old = """    @Override
    public boolean canSleep() {
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else {
            return this.level().isBrightOutside();
        }
    }
"""
    lion_original = """    @Override
    public boolean canSleep() {
        long dayTime = this.level().getDayTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else {
            return dayTime > 6000 && dayTime < 13000;
        }
    }
"""
    if replace_once(LION, lion_old, lion_original):
        changed.append(str(LION))

    komodo_old = """    @Override
    public boolean canSleep() {
        return this.level().isBrightOutside() && this.getTarget() == null && !this.level().isWaterAt(this.blockPosition());
    }
"""
    komodo_original = """    @Override
    public boolean canSleep() {
        return this.level().isDay() && this.getTarget() == null && !this.level().isWaterAt(this.blockPosition());
    }
"""
    if replace_once(KOMODO, komodo_old, komodo_original):
        changed.append(str(KOMODO))

    print(f"26.2 behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
