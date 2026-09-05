#!/usr/bin/env python3
"""Restore Ostrich's custom 1.21.1 egg-defense anger countdown on Minecraft 26.2.

Naturalist intentionally paused the remaining anger duration while the ostrich was actively
engaged with the same target inside follow range, then resumed the same remaining duration after
engagement ended. Minecraft 26.2's NeutralMob#updatePersistentAnger(level, true) instead refreshes
the timer whenever a target is present. Translate the old paused countdown to the new absolute
anger-end-time representation by extending the end time by one tick for each engaged tick.
"""

from pathlib import Path
import runpy

PATH = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Ostrich.java")


def replace_method(text: str, marker: str, replacement: str) -> str:
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f"Could not locate {marker!r}")
    brace = text.find("{", start)
    depth = 0
    for i in range(brace, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return text[:start] + replacement + text[i + 1:]
    raise RuntimeError(f"Unterminated method {marker!r}")


def main() -> None:
    text = PATH.read_text(encoding="utf-8")
    desired = """    private void updateEggAnger() {
        long angerEndTime = this.getPersistentAngerEndTime();
        if (angerEndTime <= 0L) {
            return;
        }
        LivingEntity target = this.getTarget();
        EntityReference<LivingEntity> angerTarget = this.getPersistentAngerTarget();
        boolean engaged = target != null && target.isAlive()
                && angerTarget != null && angerTarget.matches(target)
                && this.closerThan(target, this.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (engaged) {
            // 1.21.1 did not decrement RemainingPersistentAngerTime while actively engaged.
            // Since 26.2 stores an absolute end time, move it forward one tick to pause it.
            this.setPersistentAngerEndTime(angerEndTime + 1L);
        } else if (angerEndTime <= this.level().getGameTime()) {
            this.stopBeingAngry();
        }
    }
"""
    start = text.find("    private void updateEggAnger() {")
    if start < 0:
        raise RuntimeError("Could not locate Ostrich updateEggAnger")
    current = replace_method(text, "    private void updateEggAnger() {", desired)
    if current == text:
        print("26.2 Ostrich anger parity pass changed 0 files")
    else:
        PATH.write_text(current, encoding="utf-8")
        print(f"26.2 Ostrich anger parity pass changed {PATH}")


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_23.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
