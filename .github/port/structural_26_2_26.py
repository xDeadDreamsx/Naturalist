#!/usr/bin/env python3
"""Restore Naturalist's legacy getEatingSound(ItemStack) calls on Minecraft 26.2.

The old LivingEntity helper returned the stack's normal generic eating sound for these Naturalist
call sites. An early migration replaced every playSound(getEatingSound(...), 1, 1) with
Animal#playEatingSound(), but that base 26.2 method is intentionally empty. Replace only the exact
no-argument calls introduced by that migration with the equivalent generic eating sound.
"""
from pathlib import Path
import runpy

ROOT = Path("common/src/main/java/com/crispytwig/naturalist")
OLD = "this.playEatingSound();"
NEW = "this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);"


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    positions = [i for i in range(len(text)) if text.startswith("import ", i)]
    if not positions:
        return text
    end = text.find("\n", positions[-1]) + 1
    return text[:end] + line + text[end:]


def main() -> None:
    changed: list[str] = []
    for path in ROOT.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        if OLD not in text:
            continue
        text = text.replace(OLD, NEW)
        text = add_import(text, "net.minecraft.sounds.SoundEvents")
        path.write_text(text, encoding="utf-8")
        changed.append(str(path))

    print(f"26.2 eating-sound parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    runpy.run_path(".github/port/structural_26_2_27.py", run_name="__main__")
    runpy.run_path(".github/port/structural_26_2_29.py", run_name="__main__")
