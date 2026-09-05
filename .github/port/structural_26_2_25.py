#!/usr/bin/env python3
"""Preserve dynamic ItemStack components in Naturalist item particles on Minecraft 26.2.

An early migration converted old ItemParticleOption(..., ItemStack) uses to .getItem(), losing
stack component data. Minecraft 26.2 ItemParticleOption accepts ItemStackTemplate, which can retain
the complete non-empty stack patch. Restore only the variable patterns that the old migration
itself introduced, leaving intentionally static item particles untouched.
"""

from pathlib import Path

ROOT = Path("common/src/main/java")
VARIABLES = ("eatingStack", "itemStack", "particleStack", "stack")


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
        original = text
        for var in VARIABLES:
            text = text.replace(
                f"new ItemParticleOption(ParticleTypes.ITEM, {var}.getItem())",
                f"new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack({var}))",
            )
        if text != original:
            text = add_import(text, "net.minecraft.world.item.ItemStackTemplate")
            path.write_text(text, encoding="utf-8")
            changed.append(str(path))

    print(f"26.2 item-particle component parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
