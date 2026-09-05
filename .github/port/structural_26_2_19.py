#!/usr/bin/env python3
"""Restore Butterfly's explicit adult-only state from Naturalist 1.21.1.

Butterflies breed into caterpillars rather than producing baby butterflies. The original class
therefore overrides isBaby() to always return false. That override was dropped by the 26.2 port.
"""

from pathlib import Path

BUTTERFLY = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Butterfly.java")


def main() -> None:
    text = BUTTERFLY.read_text(encoding="utf-8")
    method = """    @Override
    public boolean isBaby() {
        return false;
    }
"""
    if method in text:
        print("26.2 Butterfly behavior parity pass changed 0 files")
        return

    anchor = """    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return NaturalistEntityTypes.CATERPILLAR.get().create(serverLevel, EntitySpawnReason.BREEDING);
    }
"""
    if anchor not in text:
        raise RuntimeError("Could not locate Butterfly breed-offspring insertion point")
    BUTTERFLY.write_text(text.replace(anchor, anchor + "\n" + method, 1), encoding="utf-8")
    print(f"26.2 Butterfly behavior parity pass changed {BUTTERFLY}")


if __name__ == "__main__":
    main()
