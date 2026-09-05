#!/usr/bin/env python3
"""Restore additional Naturalist 1.21.1 movement and interaction semantics on 26.2.

Minecraft 26.2 moved PathfinderMob's old restriction API to Mob home methods and routes ridden
movement through LivingEntity's ridden hooks. It also keeps pass-door configuration on the
NodeEvaluator rather than PathNavigation. Restore Naturalist's original intent using those direct
26.2 successors, narrow Crab weapon recognition to the original sword/digger categories, and
restore the Vulture's original player detection distance.
"""

from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob")


def replace_method(path: Path, marker: str, replacement: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if replacement in text:
        return False
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f"Could not locate {marker!r} in {path}")
    brace = text.find("{", start)
    if brace < 0:
        raise RuntimeError(f"Could not locate body for {marker!r} in {path}")
    depth = 0
    for i in range(brace, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                path.write_text(text[:start] + replacement + text[i + 1:], encoding="utf-8")
                return True
    raise RuntimeError(f"Unterminated method {marker!r} in {path}")


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    marker = "\nimport "
    last = text.rfind(marker)
    if last >= 0:
        end = text.find("\n", last + 1) + 1
        return text[:end] + line + text[end:]
    package_end = text.find("\n", text.find("package ")) + 1
    return text[:package_end] + "\n" + line + text[package_end:]


def main() -> None:
    changed = []

    rat = ROOT / "Rat.java"
    rat_workstation = """    public void setWorkstation(@Nullable BlockPos pos) {
        this.workstationPos = pos == null ? null : pos.immutable();
        if (this.workstationPos != null) {
            this.setHomeTo(this.workstationPos, WORK_RADIUS);
        } else {
            this.clearHome();
        }
    }
"""
    if replace_method(rat, "    public void setWorkstation(@Nullable BlockPos pos) {", rat_workstation):
        changed.append(str(rat))

    giraffe = ROOT / "Giraffe.java"
    # A failed earlier run committed a generated file containing duplicate @Override annotations.
    # Repair that state first so the pass is idempotent on both pre- and post-migration branches.
    giraffe_text = giraffe.read_text(encoding="utf-8")
    duplicate_override = "    @Override\n    @Override\n    protected void tickRidden"
    if duplicate_override in giraffe_text:
        giraffe_text = giraffe_text.replace(duplicate_override, "    @Override\n    protected void tickRidden", 1)
        giraffe.write_text(giraffe_text, encoding="utf-8")
        changed.append(str(giraffe))

    # The existing @Override immediately before the old travel() method remains outside the
    # replacement range, so the first replacement method intentionally starts without another
    # annotation. Subsequent methods need their own annotations.
    giraffe_riding = """    protected void tickRidden(@NotNull Player controller, @NotNull Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        this.setYRot(controller.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(controller.getXRot() * 0.5F);
        this.setRot(this.getYRot(), this.getXRot());
        this.yHeadRot = this.getYRot();
        this.yBodyRot = Mth.rotLerp(0.35F, this.yBodyRot, this.getYRot());
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(@NotNull Player controller, @NotNull Vec3 selfInput) {
        return new Vec3(controller.xxa * 0.5F, 0.0D, controller.zza);
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player controller) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }
"""
    if "protected void tickRidden(@NotNull Player controller" not in giraffe.read_text(encoding="utf-8"):
        if replace_method(giraffe, "    public void travel(@NotNull Vec3 travelVector) {", giraffe_riding):
            if str(giraffe) not in changed:
                changed.append(str(giraffe))

    crab = ROOT / "Crab.java"
    crab_text = crab.read_text(encoding="utf-8")
    crab_old = """    private static boolean isWeapon(ItemStack stack) {
        return stack.has(DataComponents.TOOL);
    }
"""
    crab_new = """    private static boolean isWeapon(ItemStack stack) {
        return stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.SHOVELS);
    }
"""
    if crab_new not in crab_text:
        if crab_old not in crab_text:
            raise RuntimeError("Could not locate Crab weapon predicate")
        crab_text = crab_text.replace(crab_old, crab_new, 1)
        crab_text = add_import(crab_text, "net.minecraft.tags.ItemTags")
        crab.write_text(crab_text, encoding="utf-8")
        changed.append(str(crab))

    for name in ("Bird.java", "Butterfly.java", "Firefly.java", "Vulture.java"):
        path = ROOT / name
        text = path.read_text(encoding="utf-8")
        exact = "        navigation.getNodeEvaluator().setCanPassDoors(true);\n"
        if exact in text:
            continue
        anchor = "        navigation.setCanFloat("
        start = text.find(anchor)
        if start < 0:
            raise RuntimeError(f"Could not locate navigation float setting in {path}")
        end = text.find("\n", start)
        text = text[:end + 1] + exact + text[end + 1:]
        path.write_text(text, encoding="utf-8")
        changed.append(str(path))

    vulture = ROOT / "Vulture.java"
    text = vulture.read_text(encoding="utf-8")
    old = "this.vulture.getZ(), 8.0D, entity -> entity instanceof Player player && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player))"
    new = "this.vulture.getZ(), 16.0D, entity -> entity instanceof Player player && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player))"
    if old in text:
        text = text.replace(old, new, 1)
        vulture.write_text(text, encoding="utf-8")
        if str(vulture) not in changed:
            changed.append(str(vulture))

    print(f"26.2 movement/pathing parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
