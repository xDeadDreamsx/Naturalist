#!/usr/bin/env python3
"""Restore Naturalist 1.21.1 time-of-day behavior after the Minecraft 26.2 API port.

Minecraft 26.x renamed the old Level#isDay/isNight checks to isBrightOutside/isDarkOutside,
while getDayTime moved to the overworld WorldClock. Earlier migration waves sometimes replace
Naturalist's custom tick windows with generic brightness checks. This final parity pass runs after
those waves and rewrites the affected methods by structure, so it remains stable even if an older
migration changes whitespace or formatting.
"""

from pathlib import Path
import runpy

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity")


def replace_method(path: Path, signature: str, desired: str) -> bool:
    text = path.read_text(encoding="utf-8")
    start = text.find(signature)
    if start < 0:
        raise RuntimeError(f"Method signature {signature!r} not found in {path}")
    brace = text.find("{", start)
    if brace < 0:
        raise RuntimeError(f"Opening brace not found for {signature!r} in {path}")
    depth = 0
    end = None
    for i in range(brace, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                end = i + 1
                break
    if end is None:
        raise RuntimeError(f"Closing brace not found for {signature!r} in {path}")
    current = text[start:end]
    if current == desired:
        return False
    path.write_text(text[:start] + desired + text[end:], encoding="utf-8")
    return True


def replace_text(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        return False
    path.write_text(text.replace(old, new), encoding="utf-8")
    return True


def main() -> None:
    changed: list[str] = []

    methods = {
        ROOT / "mob/Bear.java": """    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        return this.wakeTicks <= 0 && (dayTime < 12000 || dayTime > 18000) && dayTime < 23000 && dayTime > 6000 && !this.isAngry() && !this.level().isWaterAt(this.blockPosition());
    }""",
        ROOT / "mob/Capybara.java": """    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime() % 24000;
        return dayTime > 6000 && dayTime < 13000 && this.onGround() && !this.isInWater()
                && !this.isOrderedToSit() && !this.isInLove() && this.getLastHurtByMob() == null;
    }""",
        ROOT / "mob/Lion.java": """    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else {
            return dayTime > 6000 && dayTime < 13000;
        }
    }""",
        ROOT / "mob/Snake.java": """    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else if (dayTime > 18000 && dayTime < 23000) {
            return false;
        } else return dayTime > 12000 && dayTime < 28000;
    }""",
        ROOT / "mob/Tiger.java": """    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return dayTime > 6000 && dayTime < 13000;
    }""",
        # Level#isDay was renamed to isBrightOutside in the current mappings, so Komodo's
        # original behavior already has a direct 26.x equivalent.
        ROOT / "mob/KomodoDragon.java": """    public boolean canSleep() {
        return this.level().isBrightOutside() && this.getTarget() == null && !this.level().isWaterAt(this.blockPosition());
    }""",
    }
    for path, desired in methods.items():
        if replace_method(path, "    public boolean canSleep() {", desired):
            changed.append(str(path))

    # isDarkOutside is the direct 26.x successor of the old isNight method. Using !isBrightOutside
    # is subtly different around transition/weather states, so restore the original distinction.
    nocturnal = ROOT / "base/NocturnalHostile.java"
    if replace_text(nocturnal,
                    "return !((LivingEntity) this).level().isBrightOutside();",
                    "return ((LivingEntity) this).level().isDarkOutside();"):
        changed.append(str(nocturnal))

    bird = ROOT / "mob/Bird.java"
    bird_changed = False
    bird_changed |= replace_text(bird, "if (!this.level().isBrightOutside()) {", "if (this.level().isDarkOutside()) {")
    bird_changed |= replace_text(bird, "if (this.level() instanceof ServerLevel serverLevel && !!this.level().isBrightOutside()) {",
                                 "if (this.level() instanceof ServerLevel serverLevel && !this.level().isDarkOutside()) {")
    if bird_changed:
        changed.append(str(bird))

    firefly = ROOT / "mob/Firefly.java"
    if replace_text(firefly,
                    "return !this.level().isBrightOutside() || this.level().getMaxLocalRawBrightness(this.blockPosition()) < 8;",
                    "return this.level().isDarkOutside() || this.level().getMaxLocalRawBrightness(this.blockPosition()) < 8;"):
        changed.append(str(firefly))

    print(f"26.2 behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    for next_pass in (
            Path(".github/port/structural_26_2_13.py"),
            Path(".github/port/structural_26_2_14.py")):
        if next_pass.exists():
            runpy.run_path(str(next_pass), run_name="__main__")
