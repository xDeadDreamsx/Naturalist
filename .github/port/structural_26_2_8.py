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


def _matching_paren(text: str, open_paren: int) -> int:
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
                return index
    raise RuntimeError("Unbalanced parenthesis")


def _registration_end(text: str, start: int) -> int:
    open_paren = text.find("(", start)
    if open_paren < 0:
        raise RuntimeError("Could not locate registration opening parenthesis")
    return _matching_paren(text, open_paren) + 1


def patch_item_ids(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text

    # Minecraft 26.2 requires Item.Properties.setId(ResourceKey<Item>) before the Item
    # constructor runs. Apply it once to every literal Naturalist ITEMS registration and
    # also clean duplicate setId calls left by earlier migration-pass runs.
    matches = list(re.finditer(r'ITEMS\.register\("([^"\\]+)"\s*,', text))
    for match in reversed(matches):
        name = match.group(1)
        end = _registration_end(text, match.start())
        chunk = text[match.start():end]
        replacement = (
            "new Item.Properties().setId(ResourceKey.create(Registries.ITEM, "
            f'Naturalist.location("{name}")))'
        )
        set_id_suffix = replacement[len("new Item.Properties()"):]
        duplicate = replacement + set_id_suffix
        while duplicate in chunk:
            chunk = chunk.replace(duplicate, replacement)

        if replacement not in chunk and "new Item.Properties()" in chunk:
            chunk = chunk.replace("new Item.Properties()", replacement)
        text = text[:match.start()] + chunk + text[end:]

    # Block-item helper registrations use a variable name rather than a string literal.
    helper_replacement = (
        "new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Naturalist.location(name)))"
    )
    helper_suffix = helper_replacement[len("new Item.Properties()"):]
    while helper_replacement + helper_suffix in text:
        text = text.replace(helper_replacement + helper_suffix, helper_replacement)
    text = text.replace(
        "ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));",
        f"ITEMS.register(name, () -> new BlockItem(holder.get(), {helper_replacement}));",
    )

    if text == original:
        return False
    path.write_text(text, encoding="utf-8")
    return True


def _inject_block_id_into_properties(chunk: str, key_expression: str) -> str:
    marker = "BlockBehaviour.Properties."
    offset = 0
    while True:
        start = chunk.find(marker, offset)
        if start < 0:
            break

        factory_open = chunk.find("(", start + len(marker))
        if factory_open < 0:
            break
        factory_close = _matching_paren(chunk, factory_open)
        suffix = chunk[factory_close + 1:]
        if suffix.startswith(".setId("):
            offset = factory_close + 1
            continue

        insertion = f".setId({key_expression})"
        chunk = chunk[:factory_close + 1] + insertion + chunk[factory_close + 1:]
        offset = factory_close + 1 + len(insertion)
    return chunk


def patch_block_ids(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text

    # 26.2 also requires BlockBehaviour.Properties.setId(ResourceKey<Block>) before the
    # block constructor executes. Cover both normal and block-only literal registrations.
    matches = list(re.finditer(r'(?:registerBlock|registerBlockOnly)\("([^"\\]+)"\s*,', text))
    for match in reversed(matches):
        name = match.group(1)
        end = _registration_end(text, match.start())
        chunk = text[match.start():end]
        if "BlockBehaviour.Properties." not in chunk:
            continue
        key_expression = f'ResourceKey.create(Registries.BLOCK, Naturalist.location("{name}"))'
        patched = _inject_block_id_into_properties(chunk, key_expression)
        text = text[:match.start()] + patched + text[end:]

    # Starfish registrations are generated through a helper whose block id is the `name`
    # parameter rather than a literal at each callsite.
    starfish_marker = "private static DeferredHolder<Block, StarfishBlock> registerStarfishBlock(String name)"
    helper_start = text.find(starfish_marker)
    if helper_start >= 0:
        helper_end = text.find("\n    }", helper_start)
        if helper_end >= 0:
            helper_end += len("\n    }")
            chunk = text[helper_start:helper_end]
            key_expression = "ResourceKey.create(Registries.BLOCK, Naturalist.location(name))"
            patched = _inject_block_id_into_properties(chunk, key_expression)
            text = text[:helper_start] + patched + text[helper_end:]

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
    if patch_block_ids(registry):
        changed.append(str(registry))
    print(f"26.2 structural pass 8 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
