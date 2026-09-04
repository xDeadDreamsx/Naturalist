from pathlib import Path


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


def main() -> None:
    changed = []
    snake = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Snake.java")
    knapsack = Path("common/src/main/java/com/crispytwig/naturalist/server/item/KnapsackItem.java")
    if patch_snake(snake):
        changed.append(str(snake))
    if patch_knapsack(knapsack):
        changed.append(str(knapsack))
    print(f"26.2 structural pass 8 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
