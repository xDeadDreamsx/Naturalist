#!/usr/bin/env python3
"""Restore Butterfly/Caterpillar age and scale semantics from Naturalist 1.21.1.

Butterflies breed into caterpillars rather than producing baby butterflies, so the original
Butterfly is permanently adult. Caterpillar deliberately keeps a physical age scale of 1.0 while
its AgeableMob age still controls maturation toward a cocoon. Minecraft 26.2 made isBaby() and
getScale() final; canBeABaby() and getAgeScale() are the supported replacement hooks.
"""

from pathlib import Path
import re
import runpy

BUTTERFLY = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Butterfly.java")
CATERPILLAR = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Caterpillar.java")


def main() -> None:
    changed: list[str] = []

    text = BUTTERFLY.read_text(encoding="utf-8")
    original = text
    text = re.sub(
        r"\n\s*@Override\s*\n\s*public boolean isBaby\(\) \{\s*return false;\s*\}\s*\n",
        "\n",
        text,
        count=1,
    )
    adult_hook = """    @Override
    protected boolean canBeABaby() {
        return false;
    }
"""
    if adult_hook not in text:
        anchor = """    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.CATERPILLAR.get().create(serverLevel, EntitySpawnReason.BREEDING);
    }
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Butterfly breed-offspring insertion point")
        text = text.replace(anchor, anchor + "\n" + adult_hook, 1)
    if text != original:
        BUTTERFLY.write_text(text, encoding="utf-8")
        changed.append(str(BUTTERFLY))

    text = CATERPILLAR.read_text(encoding="utf-8")
    original = text
    # Remove the incompatible direct getScale override from the first parity attempt, if present.
    text = re.sub(
        r"\n\s*@Override\s*\n\s*public float getScale\(\) \{\s*return 1\.0F;\s*\}\s*\n",
        "\n",
        text,
        count=1,
    )
    scale_hook = """    @Override
    public float getAgeScale() {
        return 1.0F;
    }
"""
    if scale_hook not in text:
        anchor = """    @Override
    protected float getClimbSpeedMultiplier() {
        return 0.5F;
    }
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Caterpillar scale insertion point")
        text = text.replace(anchor, anchor + "\n" + scale_hook, 1)
    if text != original:
        CATERPILLAR.write_text(text, encoding="utf-8")
        changed.append(str(CATERPILLAR))

    print(f"26.2 Butterfly/Caterpillar behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_20.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
