from pathlib import Path
import re

ROOT = Path("common/src/main/java")

# Minecraft 26.2 no longer allows resolving registry tags while the built-in registries are
# still being bootstrapped. Several Naturalist mob classes used static Ingredient fields such
# as Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(NaturalistTags.ItemTags.CRAB_FOOD)).
# Entity classes are initialized while their EntityType suppliers are registered, before tags
# are bound, so defer those Ingredient lookups until the mob is actually using them.
PATTERN = re.compile(
    r'(?P<indent>^[ \t]*)private static final Ingredient (?P<name>[A-Z0-9_]+) = '
    r'Ingredient\.of\(BuiltInRegistries\.ITEM\.getOrThrow\((?P<tag>NaturalistTags\.[A-Za-z0-9_.$]+)\)\);',
    re.MULTILINE,
)


def method_name(field_name: str) -> str:
    parts = field_name.lower().split("_")
    return parts[0] + "".join(part.capitalize() for part in parts[1:])


def patch_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text
    matches = list(PATTERN.finditer(text))

    for match in reversed(matches):
        field = match.group("name")
        tag = match.group("tag")
        indent = match.group("indent")
        method = method_name(field)
        replacement = (
            f"{indent}private static Ingredient {method}() {{\n"
            f"{indent}    return Ingredient.of(BuiltInRegistries.ITEM.getOrThrow({tag}));\n"
            f"{indent}}}"
        )
        text = text[:match.start()] + replacement + text[match.end():]

        # All former field reads now become deferred calls. registerGoals and isFood are only
        # reached after datapack/tag loading, unlike class initialization during registry setup.
        text = re.sub(rf'\b{re.escape(field)}\b', f"{method}()", text)

    if text == original:
        return False
    path.write_text(text, encoding="utf-8")
    return True


def main() -> None:
    changed = []
    for path in ROOT.rglob("*.java"):
        if patch_file(path):
            changed.append(str(path))
    print(f"26.2 structural pass 9 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
