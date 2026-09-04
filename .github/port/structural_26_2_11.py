#!/usr/bin/env python3
"""Final 26.2 Java cleanup for bug-net recipe semantics.

Minecraft 26.2 decodes recipe result stacks as ItemStackTemplate during datapack loading.
Older Naturalist migration waves still rebuilt the bug-net recipe body with ItemStack.copy()
and without its special-recipe marker, so enforce both after all legacy Java passes have run.
"""

from pathlib import Path

RECIPE = Path("common/src/main/java/com/crispytwig/naturalist/server/recipe/BugNetInteractionRecipe.java")
BUG_NET_ITEM = Path("common/src/main/java/com/crispytwig/naturalist/server/item/BugNetItem.java")


def patch_recipe(text: str) -> str:
    text = text.replace("return dropStack.copy();", "return dropStack.create();")
    if "public boolean isSpecial()" not in text:
        marker = "    @Override\n    public boolean showNotification() {"
        special = (
            "    @Override\n"
            "    public boolean isSpecial() {\n"
            "        return true;\n"
            "    }\n\n"
        )
        text = text.replace(marker, special + marker, 1)
    return text


def main() -> None:
    changed = []

    if RECIPE.exists():
        text = RECIPE.read_text(encoding="utf-8")
        migrated = patch_recipe(text)
        if migrated != text:
            RECIPE.write_text(migrated, encoding="utf-8")
            changed.append(str(RECIPE))

    if BUG_NET_ITEM.exists():
        text = BUG_NET_ITEM.read_text(encoding="utf-8")
        migrated = text.replace(".dropStack().copy()", ".dropStack().create()")
        if migrated != text:
            BUG_NET_ITEM.write_text(migrated, encoding="utf-8")
            changed.append(str(BUG_NET_ITEM))

    print(f"26.2 structural pass 11 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
