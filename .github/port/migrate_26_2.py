from pathlib import Path
import re

ROOTS = [Path("common/src/main/java"), Path("fabric/src/main/java")]

DIRECT_REPLACEMENTS = {
    "import net.minecraft.world.level.GameRules;": "import net.minecraft.world.level.gamerules.GameRules;",
    "import net.minecraft.advancements.CriteriaTriggers;": "import net.minecraft.advancements.triggers.CriteriaTriggers;",
    "import net.minecraft.client.renderer.RenderType;": "import net.minecraft.client.renderer.rendertype.RenderTypes;",
    "RenderType::entityCutout": "RenderTypes::entityCutoutCull",
    "RenderType::entityTranslucent": "RenderTypes::entityTranslucent",
    "import net.minecraft.world.entity.vehicle.Boat;": "import net.minecraft.world.entity.vehicle.boat.Boat;",
    "import net.minecraft.world.entity.projectile.ThrowableItemProjectile;": "import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;",
    "import net.minecraft.world.effect.InstantenousMobEffect;": "import net.minecraft.world.effect.InstantaneousMobEffect;",
    "InstantenousMobEffect": "InstantaneousMobEffect",
    "import net.minecraft.client.player.Input;": "import net.minecraft.world.entity.player.Input;",
    "import net.minecraft.advancements.critereon.*;": "import net.minecraft.advancements.predicates.*;\nimport net.minecraft.advancements.triggers.*;",
    "import net.minecraft.world.InteractionResultHolder;": "import net.minecraft.world.InteractionResult;",
    "import net.minecraft.world.ItemInteractionResult;": "import net.minecraft.world.InteractionResult;",
    "ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION": "InteractionResult.TRY_WITH_EMPTY_HAND",
    "ItemInteractionResult.SUCCESS": "InteractionResult.SUCCESS",
    "ItemInteractionResult.PASS": "InteractionResult.PASS",
    "ItemInteractionResult.FAIL": "InteractionResult.FAIL",
    "ItemInteractionResult": "InteractionResult",
    "player.getCooldowns().isOnCooldown(stack.getItem())": "player.getCooldowns().isOnCooldown(stack)",
    "player.getCooldowns().addCooldown(stack.getItem(), 20)": "player.getCooldowns().addCooldown(stack, 20)",
    # Repair the one nested generic transformed by the first migration pass.
    "CallbackInfoReturnable<InteractionResult cir)": "CallbackInfoReturnable<InteractionResult> cir)",
}


def replace_balanced_call(text: str, prefix: str, replacement: str) -> str:
    start = 0
    while True:
        idx = text.find(prefix, start)
        if idx < 0:
            return text
        open_idx = idx + len(prefix) - 1
        depth = 0
        pos = open_idx
        in_string = False
        quote = ""
        escaped = False
        while pos < len(text):
            ch = text[pos]
            if in_string:
                if escaped:
                    escaped = False
                elif ch == "\\":
                    escaped = True
                elif ch == quote:
                    in_string = False
            else:
                if ch in ('"', "'"):
                    in_string = True
                    quote = ch
                elif ch == "(":
                    depth += 1
                elif ch == ")":
                    depth -= 1
                    if depth == 0:
                        text = text[:idx] + replacement + text[pos + 1:]
                        start = idx + len(replacement)
                        break
            pos += 1
        else:
            return text


def migrate_text(text: str) -> str:
    migrated = text
    for old, new in DIRECT_REPLACEMENTS.items():
        migrated = migrated.replace(old, new)

    migrated = migrated.replace("import net.minecraft.client.renderer.LightTexture;\n", "")

    # ResourceKey.location() was renamed to identifier() in current Mojang mappings.
    migrated = migrated.replace(".location()", ".identifier()")

    # Level#isClientSide is a method in 26.2 rather than a public field.
    migrated = re.sub(r"\.isClientSide(?!\s*\()", ".isClientSide()", migrated)

    # InteractionResultHolder and ItemInteractionResult were folded into InteractionResult.
    # Only consume a single, non-nested generic argument; outer generics must stay intact.
    migrated = re.sub(r"\bInteractionResultHolder\s*<[^<>]+>", "InteractionResult", migrated)
    for prefix, replacement in (
        ("InteractionResultHolder.sidedSuccess(", "InteractionResult.SUCCESS"),
        ("InteractionResultHolder.success(", "InteractionResult.SUCCESS"),
        ("InteractionResultHolder.consume(", "InteractionResult.CONSUME"),
        ("InteractionResultHolder.pass(", "InteractionResult.PASS"),
        ("InteractionResultHolder.fail(", "InteractionResult.FAIL"),
        ("InteractionResult.sidedSuccess(", "InteractionResult.SUCCESS"),
        ("ItemInteractionResult.sidedSuccess(", "InteractionResult.SUCCESS"),
    ):
        migrated = replace_balanced_call(migrated, prefix, replacement)

    # 26.2 Player message split: old displayClientMessage(..., true) was overlay/action-bar text.
    migrated = re.sub(
        r"player\.displayClientMessage\((.*?),\s*true\);",
        r"player.sendOverlayMessage(\1);",
        migrated,
        flags=re.DOTALL,
    )

    return migrated


def main() -> None:
    changed = []
    for root in ROOTS:
        if not root.exists():
            continue
        for path in root.rglob("*.java"):
            text = path.read_text(encoding="utf-8")
            migrated = migrate_text(text)
            if migrated != text:
                path.write_text(migrated, encoding="utf-8")
                changed.append(str(path))

    print(f"26.2 migration pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
