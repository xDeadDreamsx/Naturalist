#!/usr/bin/env python3
"""Restore Naturalist 1.21.1 time-of-day behavior after the Minecraft 26.2 API port.

Minecraft 26.x renamed the old Level#isDay/isNight checks to isBrightOutside/isDarkOutside,
while getDayTime moved to the overworld WorldClock. Earlier migration waves sometimes replaced
Naturalist's custom tick windows with a generic bright/dark check, which changes sleeping and
activity behavior. Preserve the original Naturalist predicates and only translate the API surface.
"""

from pathlib import Path
import runpy

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity")


def replace_variants(path: Path, variants: list[str], desired: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if desired in text:
        return False
    for old in variants:
        if old in text:
            path.write_text(text.replace(old, desired, 1), encoding="utf-8")
            return True
    raise RuntimeError(f"Expected behavior-migration pattern not found in {path}")


def replace_text(path: Path, old: str, new: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        return False
    path.write_text(text.replace(old, new), encoding="utf-8")
    return True


def main() -> None:
    changed: list[str] = []

    bear = ROOT / "mob/Bear.java"
    bear_desired = """    @Override
    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        return this.wakeTicks <= 0 && (dayTime < 12000 || dayTime > 18000) && dayTime < 23000 && dayTime > 6000 && !this.isAngry() && !this.level().isWaterAt(this.blockPosition());
    }
"""
    if replace_variants(bear, [
        """    @Override
    public boolean canSleep() {
        return this.wakeTicks <= 0 && !this.level().isBrightOutside() && !this.isAngry() && !this.level().isWaterAt(this.blockPosition());
    }
""",
        bear_desired.replace("getOverworldClockTime", "getDayTime"),
    ], bear_desired):
        changed.append(str(bear))

    capybara = ROOT / "mob/Capybara.java"
    capybara_desired = """    @Override
    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime() % 24000;
        return dayTime > 6000 && dayTime < 13000 && this.onGround() && !this.isInWater()
                && !this.isOrderedToSit() && !this.isInLove() && this.getLastHurtByMob() == null;
    }
"""
    if replace_variants(capybara, [
        """    @Override
    public boolean canSleep() {
        return this.level().isBrightOutside() && this.onGround() && !this.isInWater()
                && !this.isOrderedToSit() && !this.isInLove() && this.getLastHurtByMob() == null;
    }
""",
        capybara_desired.replace("getOverworldClockTime", "getDayTime"),
    ], capybara_desired):
        changed.append(str(capybara))

    lion = ROOT / "mob/Lion.java"
    lion_desired = """    @Override
    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else {
            return dayTime > 6000 && dayTime < 13000;
        }
    }
"""
    if replace_variants(lion, [
        """    @Override
    public boolean canSleep() {
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else {
            return this.level().isBrightOutside();
        }
    }
""",
        lion_desired.replace("getOverworldClockTime", "getDayTime"),
    ], lion_desired):
        changed.append(str(lion))

    snake = ROOT / "mob/Snake.java"
    snake_desired = """    @Override
    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else if (dayTime > 18000 && dayTime < 23000) {
            return false;
        } else return dayTime > 12000 && dayTime < 28000;
    }
"""
    if replace_variants(snake, [
        """    @Override
    public boolean canSleep() {
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return !this.level().isBrightOutside();
    }
""",
        snake_desired.replace("getOverworldClockTime", "getDayTime"),
    ], snake_desired):
        changed.append(str(snake))

    tiger = ROOT / "mob/Tiger.java"
    tiger_desired = """    @Override
    public boolean canSleep() {
        long dayTime = this.level().getOverworldClockTime();
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return dayTime > 6000 && dayTime < 13000;
    }
"""
    if replace_variants(tiger, [
        """    @Override
    public boolean canSleep() {
        if (this.isTame() || this.getTarget() != null || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return this.level().isBrightOutside();
    }
""",
        tiger_desired.replace("getOverworldClockTime", "getDayTime"),
    ], tiger_desired):
        changed.append(str(tiger))

    # In Mojang/Yarn mappings these are the direct 26.x successors of the old isNight checks.
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

    # Komodo Dragon's original isDay() is a direct API rename in 26.x; keep the existing
    # behaviorally equivalent isBrightOutside() rather than broadening/narrowing it manually.
    komodo = ROOT / "mob/KomodoDragon.java"
    if replace_text(komodo,
                    "return this.level().isDay() && this.getTarget() == null && !this.level().isWaterAt(this.blockPosition());",
                    "return this.level().isBrightOutside() && this.getTarget() == null && !this.level().isWaterAt(this.blockPosition());"):
        changed.append(str(komodo))

    print(f"26.2 behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_13.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
