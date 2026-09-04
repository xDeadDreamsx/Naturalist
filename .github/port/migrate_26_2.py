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


def find_matching_brace(text: str, open_idx: int) -> int | None:
    depth = 0
    pos = open_idx
    in_string = False
    quote = ""
    escaped = False
    line_comment = False
    block_comment = False
    while pos < len(text):
        ch = text[pos]
        nxt = text[pos + 1] if pos + 1 < len(text) else ""
        if line_comment:
            if ch == "\n":
                line_comment = False
        elif block_comment:
            if ch == "*" and nxt == "/":
                block_comment = False
                pos += 1
        elif in_string:
            if escaped:
                escaped = False
            elif ch == "\\":
                escaped = True
            elif ch == quote:
                in_string = False
        else:
            if ch == "/" and nxt == "/":
                line_comment = True
                pos += 1
            elif ch == "/" and nxt == "*":
                block_comment = True
                pos += 1
            elif ch in ('"', "'"):
                in_string = True
                quote = ch
            elif ch == "{":
                depth += 1
            elif ch == "}":
                depth -= 1
                if depth == 0:
                    return pos
        pos += 1
    return None


def add_import(text: str, qualified_name: str) -> str:
    line = f"import {qualified_name};\n"
    if line in text:
        return text
    imports = list(re.finditer(r"^import .+;\n", text, flags=re.MULTILINE))
    if imports:
        idx = imports[-1].end()
        return text[:idx] + line + text[idx:]
    package_end = text.find("\n", text.find("package ")) + 1
    return text[:package_end] + "\n" + line + text[package_end:]


def migrate_value_input_body(body: str, var: str) -> str:
    escaped = re.escape(var)
    getters = (
        ("getBoolean", "getBooleanOr", "false"),
        ("getByte", "getByteOr", "(byte) 0"),
        ("getInt", "getIntOr", "0"),
        ("getLong", "getLongOr", "0L"),
        ("getFloat", "getFloatOr", "0.0F"),
        ("getDouble", "getDoubleOr", "0.0D"),
        ("getString", "getStringOr", '""'),
    )
    for old, new, default in getters:
        body = re.sub(
            rf"\b{escaped}\.{old}\(([^()\n,]+)\)",
            rf"{var}.{new}(\1, {default})",
            body,
        )
    body = re.sub(
        rf"ContainerHelper\.loadAllItems\(\s*{escaped}\s*,\s*([^,\n]+)\s*,\s*this\.registryAccess\(\)\s*\)",
        rf"ContainerHelper.loadAllItems({var}, \1)",
        body,
    )
    return body


def migrate_value_output_body(body: str, var: str) -> str:
    escaped = re.escape(var)
    return re.sub(
        rf"ContainerHelper\.saveAllItems\(\s*{escaped}\s*,\s*([^,\n]+)\s*,\s*this\.registryAccess\(\)\s*\)",
        rf"ContainerHelper.saveAllItems({var}, \1)",
        body,
    )


def migrate_persistence_hook(text: str, method_name: str, new_type: str, is_input: bool) -> tuple[str, bool]:
    pattern = re.compile(
        rf"\b(?P<visibility>public|protected)\s+void\s+{method_name}\s*\(\s*"
        rf"(?P<annotation>@[A-Za-z0-9_$.]+(?:\([^)]*\))?\s+)?CompoundTag\s+(?P<var>[A-Za-z_$][A-Za-z0-9_$]*)\s*\)\s*\{{"
    )
    changed = False
    start = 0
    while True:
        match = pattern.search(text, start)
        if match is None:
            break
        open_idx = match.end() - 1
        close_idx = find_matching_brace(text, open_idx)
        if close_idx is None:
            break
        var = match.group("var")
        header = match.group(0).replace("CompoundTag", new_type, 1)
        body = text[open_idx + 1:close_idx]
        body = migrate_value_input_body(body, var) if is_input else migrate_value_output_body(body, var)
        replacement = header + body + "}"
        text = text[:match.start()] + replacement + text[close_idx + 1:]
        start = match.start() + len(replacement)
        changed = True
    return text, changed


def migrate_text(text: str) -> str:
    migrated = text
    for old, new in DIRECT_REPLACEMENTS.items():
        migrated = migrated.replace(old, new)

    migrated = migrated.replace("import net.minecraft.client.renderer.LightTexture;\n", "")
    migrated = migrated.replace(".location()", ".identifier()")
    migrated = re.sub(r"\.isClientSide(?!\s*\()", ".isClientSide()", migrated)

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

    migrated = re.sub(
        r"player\.displayClientMessage\((.*?),\s*true\);",
        r"player.sendOverlayMessage(\1);",
        migrated,
        flags=re.DOTALL,
    )

    migrated, changed_output = migrate_persistence_hook(migrated, "addAdditionalSaveData", "ValueOutput", False)
    migrated, changed_input = migrate_persistence_hook(migrated, "readAdditionalSaveData", "ValueInput", True)
    if changed_output:
        migrated = add_import(migrated, "net.minecraft.world.level.storage.ValueOutput")
    if changed_input:
        migrated = add_import(migrated, "net.minecraft.world.level.storage.ValueInput")

    # TagKey is no longer accepted directly by Ingredient.of; resolve it to the registry HolderSet.
    ingredient_pattern = r"Ingredient\.of\((NaturalistTags\.ItemTags\.[A-Za-z0-9_]+)\)"
    migrated, ingredient_count = re.subn(
        ingredient_pattern,
        r"Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(\1))",
        migrated,
    )
    if ingredient_count:
        migrated = add_import(migrated, "net.minecraft.core.registries.BuiltInRegistries")

    # EntityType#create(Level) was removed. Offspring creation now carries an explicit spawn reason.
    migrated = re.sub(
        r"(NaturalistEntityTypes\.[A-Z0-9_]+\.get\(\)\.create)\((serverLevel|level)\)",
        r"\1(\2, EntitySpawnReason.BREEDING)",
        migrated,
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
