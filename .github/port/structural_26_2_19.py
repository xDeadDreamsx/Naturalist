#!/usr/bin/env python3
"""Restore Butterfly/Caterpillar age and scale semantics from Naturalist 1.21.1.

Butterflies breed into caterpillars rather than producing baby butterflies, so the original
Butterfly is permanently adult. Caterpillar, meanwhile, deliberately returns a fixed scale of
1.0 so its AgeableMob age does not also alter its physical size while it matures toward a cocoon.
Both overrides were dropped during the 26.2 migration.
"""

from pathlib import Path

BUTTERFLY = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Butterfly.java")
CATERPILLAR = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Caterpillar.java")


def main() -> None:
    changed: list[str] = []

    text = BUTTERFLY.read_text(encoding="utf-8")
    method = """    @Override
    public boolean isBaby() {
        return false;
    }
"""
    if method not in text:
        anchor = """    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.CATERPILLAR.get().create(serverLevel, EntitySpawnReason.BREEDING);
    }
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Butterfly breed-offspring insertion point")
        BUTTERFLY.write_text(text.replace(anchor, anchor + "\n" + method, 1), encoding="utf-8")
        changed.append(str(BUTTERFLY))

    text = CATERPILLAR.read_text(encoding="utf-8")
    scale_method = """    @Override
    public float getScale() {
        return 1.0F;
    }
"""
    if scale_method not in text:
        anchor = """    @Override
    protected float getClimbSpeedMultiplier() {
        return 0.5F;
    }
"""
        if anchor not in text:
            raise RuntimeError("Could not locate Caterpillar scale insertion point")
        CATERPILLAR.write_text(text.replace(anchor, anchor + "\n" + scale_method, 1), encoding="utf-8")
        changed.append(str(CATERPILLAR))

    print(f"26.2 Butterfly/Caterpillar behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
