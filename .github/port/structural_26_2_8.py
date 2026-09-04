from pathlib import Path
import re


def patch_snake(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    if "getBreedOffspring(@NotNull ServerLevel serverLevel" in text:
        return False

    marker = '''    @Override
    public boolean isFood(@NotNull ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }
'''
    method = marker + '''
    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel serverLevel, @NotNull AgeableMob ageableMob) {
        return null;
    }
'''
    if marker not in text:
        raise RuntimeError("Could not locate Snake isFood insertion point")
    text = text.replace(marker, method, 1)
    path.write_text(text, encoding="utf-8")
    return True


def patch_knapsack(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    old = "EntityType.loadEntityRecursive(tag, serverLevel, EntitySpawnReason.LOAD, e -> {"
    new = "EntityType.loadEntityRecursive(tag, serverLevel, new EntitySpawnRequest(EntitySpawnReason.LOAD, true), e -> {"
    if old not in text:
        return False
    text = text.replace(old, new, 1)
    import_line = "import net.minecraft.world.entity.EntitySpawnRequest;\n"
    if import_line not in text:
        anchor = "import net.minecraft.world.entity.EntitySpawnReason;\n"
        if anchor not in text:
            raise RuntimeError("Could not locate EntitySpawnReason import")
        text = text.replace(anchor, anchor + import_line, 1)
    path.write_text(text, encoding="utf-8")
    return True


def _registration_end(text: str, start: int) -> int:
    open_paren = text.find("(", start)
    if open_paren < 0:
        raise RuntimeError("Could not locate registration opening parenthesis")
    depth = 0
    in_string = False
    escaped = False
    for index in range(open_paren, len(text)):
        char = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == '"':
                in_string = False
            continue
        if char == '"':
            in_string = True
        elif char == "(":
            depth += 1
        elif char == ")":
            depth -= 1
            if depth == 0:
                return index + 1
    raise RuntimeError("Unbalanced ITEMS.register call")


def patch_item_ids(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text

    # Minecraft 26.2 requires Item.Properties.setId(ResourceKey<Item>) before the Item
    # constructor runs. Apply it to every literal Naturalist ITEMS registration.
    matches = list(re.finditer(r'ITEMS\.register\("([^"\\]+)"\s*,', text))
    for match in reversed(matches):
        name = match.group(1)
        end = _registration_end(text, match.start())
        chunk = text[match.start():end]
        if "new Item.Properties()" not in chunk:
            continue
        replacement = (
            "new Item.Properties().setId(ResourceKey.create(Registries.ITEM, "
            f'Naturalist.location("{name}")))'
        )
        chunk = chunk.replace("new Item.Properties()", replacement)
        text = text[:match.start()] + chunk + text[end:]

    # Block-item helper registrations use a variable name rather than a string literal.
    helper_replacement = (
        "new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Naturalist.location(name)))"
    )
    text = text.replace(
        "ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));",
        f"ITEMS.register(name, () -> new BlockItem(holder.get(), {helper_replacement}));",
    )

    if text == original:
        return False
    path.write_text(text, encoding="utf-8")
    return True


def main() -> None:
    changed = []
    snake = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Snake.java")
    knapsack = Path("common/src/main/java/com/crispytwig/naturalist/server/item/KnapsackItem.java")
    registry = Path("common/src/main/java/com/crispytwig/naturalist/registry/NaturalistRegistry.java")
    if patch_snake(snake):
        changed.append(str(snake))
    if patch_knapsack(knapsack):
        changed.append(str(knapsack))
    if patch_item_ids(registry):
        changed.append(str(registry))
    print(f"26.2 structural pass 8 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
