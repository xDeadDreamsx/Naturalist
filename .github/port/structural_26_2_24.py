#!/usr/bin/env python3
"""Restore Bear/Elephant baby knockback behavior from Naturalist 1.21.1 on Minecraft 26.2.

Both mobs deliberately cancelled their high knockback-resistance attribute for babies by
pre-dividing incoming knockback strength before delegating to LivingEntity. Their overrides were
removed during the first 26.2 signature migration. Reintroduce the same calculation through the
new knockback(strength, x, z, DamageSource, sourceStrength) hook.
"""

from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob")


def insert_before(path: Path, anchor: str, method: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if method in text:
        return False
    if anchor not in text:
        raise RuntimeError(f"Could not locate insertion point in {path}")
    path.write_text(text.replace(anchor, method + "\n" + anchor, 1), encoding="utf-8")
    return True


def main() -> None:
    changed: list[str] = []

    bear = ROOT / "Bear.java"
    bear_method = """    @Override
    public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {
        if (this.isBaby()) {
            super.knockback(strength / Math.max(1.0D - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.01D),
                    x, z, source, sourceStrength);
        } else {
            super.knockback(strength, x, z, source, sourceStrength);
        }
    }
"""
    if insert_before(bear, "    @Override\n    public boolean hurtServer", bear_method):
        changed.append(str(bear))

    elephant = ROOT / "Elephant.java"
    elephant_method = """    @Override
    public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {
        if (this.isBaby()) {
            double knockbackResistance = this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            super.knockback(strength / Math.max(1.0D - knockbackResistance, 0.01D), x, z, source, sourceStrength);
        } else {
            super.knockback(strength, x, z, source, sourceStrength);
        }
    }
"""
    if insert_before(elephant, "    @Override\n    public int getMaxHeadYRot()", elephant_method):
        changed.append(str(elephant))

    print(f"26.2 Bear/Elephant knockback parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
