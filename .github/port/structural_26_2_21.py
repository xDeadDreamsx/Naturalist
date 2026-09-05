#!/usr/bin/env python3
"""Restore legacy DyeableAnimal load semantics on Minecraft 26.2.

The 1.21.1 helper only changed an animal's dye when DyeColor actually existed in saved data.
The first ValueInput port used getIntOr directly, which cleared the dye when the field was absent.
Keep the old conditional behavior so legacy/partial saves do not silently reset state.
"""

from pathlib import Path
import runpy

PATH = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/base/DyeableAnimal.java")


def main() -> None:
    text = PATH.read_text(encoding="utf-8")
    old = """    static void loadDye(DyeableAnimal animal, ValueInput input) {
        int id = input.getIntOr(\"DyeColor\", -1);
        animal.setDyeColor(id < 0 ? null : DyeColor.byId(id));
    }
"""
    new = """    static void loadDye(DyeableAnimal animal, ValueInput input) {
        if (input.contains(\"DyeColor\")) {
            int id = input.getIntOr(\"DyeColor\", -1);
            animal.setDyeColor(id < 0 ? null : DyeColor.byId(id));
        }
    }
"""
    if new in text:
        print("26.2 dye persistence parity pass changed 0 files")
    else:
        if old not in text:
            raise RuntimeError("Could not locate DyeableAnimal ValueInput loader")
        PATH.write_text(text.replace(old, new, 1), encoding="utf-8")
        print(f"26.2 dye persistence parity pass changed {PATH}")


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_22.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
