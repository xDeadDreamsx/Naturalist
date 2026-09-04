#!/usr/bin/env python3
"""Final 26.2 Java cleanup for recipe result templates.

Minecraft 26.2 decodes recipe result stacks as ItemStackTemplate during datapack loading.
Older Naturalist migration waves still rebuilt the bug-net recipe body with ItemStack.copy(),
so enforce template.create() after all legacy Java migration passes have run.
"""

from pathlib import Path

REPLACEMENTS = {
    Path("common/src/main/java/com/crispytwig/naturalist/server/recipe/BugNetInteractionRecipe.java"): (
        "return dropStack.copy();",
        "return dropStack.create();",
    ),
    Path("common/src/main/java/com/crispytwig/naturalist/server/item/BugNetItem.java"): (
        ".dropStack().copy()",
        ".dropStack().create()",
    ),
}


def main() -> None:
    changed = []
    for path, (old, new) in REPLACEMENTS.items():
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8")
        migrated = text.replace(old, new)
        if migrated != text:
            path.write_text(migrated, encoding="utf-8")
            changed.append(str(path))

    print(f"26.2 structural pass 11 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
