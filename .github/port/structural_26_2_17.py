#!/usr/bin/env python3
"""Restore shared Naturalist 1.21.1 AI/pet semantics lost during the 26.2 API port.

This pass restores three cross-cutting behaviors:
- BabySniffFlowersGoal keeps candidate flowers inside the mob's restriction/home area.
- FollowingPet preserves its existing default when old save data lacks FollowingOwner.
- PetTargeting compares persistent owner UUIDs rather than currently loaded owner entity objects.
"""

from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity")


def main() -> None:
    changed: list[str] = []

    sniff = ROOT / "ai/goal/BabySniffFlowersGoal.java"
    text = sniff.read_text(encoding="utf-8")
    old = """                    if (this.isValidTarget(this.mob.level(), mutable)) {
                        candidates.add(mutable.immutable());
                    }
"""
    new = """                    if (this.mob.isWithinHome(mutable) && this.isValidTarget(this.mob.level(), mutable)) {
                        candidates.add(mutable.immutable());
                    }
"""
    if new not in text:
        if old not in text:
            raise RuntimeError("Could not locate BabySniffFlowersGoal candidate filter")
        sniff.write_text(text.replace(old, new, 1), encoding="utf-8")
        changed.append(str(sniff))

    following = ROOT / "base/FollowingPet.java"
    text = following.read_text(encoding="utf-8")
    old = """    static void loadPet(FollowingPet pet, ValueInput input) {
        pet.setFollowingOwner(input.getBooleanOr(\"FollowingOwner\", false));
    }
"""
    new = """    static void loadPet(FollowingPet pet, ValueInput input) {
        if (input.contains(\"FollowingOwner\")) {
            pet.setFollowingOwner(input.getBooleanOr(\"FollowingOwner\", false));
        }
    }
"""
    if new not in text:
        if old not in text:
            raise RuntimeError("Could not locate FollowingPet ValueInput loader")
        following.write_text(text.replace(old, new, 1), encoding="utf-8")
        changed.append(str(following))

    targeting = ROOT / "base/PetTargeting.java"
    text = targeting.read_text(encoding="utf-8")
    old = """    public static boolean protectsOwnedPet(TamableAnimal self, LivingEntity target) {
        if (!self.isTame()) {
            return true;
        }
        LivingEntity owner = self.getOwner();
        return owner == null || !(target instanceof OwnableEntity ownable) || ownable.getOwner() != owner;
    }
"""
    new = """    public static boolean protectsOwnedPet(TamableAnimal self, LivingEntity target) {
        EntityReference<LivingEntity> owner = self.getOwnerReference();
        if (!self.isTame() || owner == null || !(target instanceof OwnableEntity ownable)) {
            return true;
        }
        EntityReference<LivingEntity> targetOwner = ownable.getOwnerReference();
        return targetOwner == null || !owner.getUUID().equals(targetOwner.getUUID());
    }
"""
    if new not in text:
        if old not in text:
            raise RuntimeError("Could not locate PetTargeting owner-protection method")
        text = text.replace(old, new, 1)
        import_line = "import net.minecraft.world.entity.EntityReference;\n"
        if import_line not in text:
            marker = "import net.minecraft.world.entity.LivingEntity;\n"
            if marker not in text:
                raise RuntimeError("Could not locate PetTargeting import insertion point")
            text = text.replace(marker, import_line + marker, 1)
        targeting.write_text(text, encoding="utf-8")
        changed.append(str(targeting))

    print(f"26.2 shared behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
