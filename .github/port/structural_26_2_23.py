#!/usr/bin/env python3
"""Restore named-mob tooltip behavior for the Knapsack on Minecraft 26.2.

Naturalist 1.21.1 preferred a captured entity's CustomName in the filled knapsack tooltip and
fell back to the entity type description. The first 26.2 migration dropped the CustomName branch
because the old JSON component parser disappeared. Decode the saved component with the modern
ComponentSerialization codec and registry-aware NBT ops instead.
"""

from pathlib import Path
import runpy

PATH = Path("common/src/main/java/com/crispytwig/naturalist/server/item/KnapsackItem.java")


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    imports = [i for i in range(len(text)) if text.startswith("import ", i)]
    if not imports:
        return text
    last = imports[-1]
    end = text.find("\n", last) + 1
    return text[:end] + line + text[end:]


def main() -> None:
    text = PATH.read_text(encoding="utf-8")
    old = """        Component label = null;
        String entityId = tag.getStringOr(\"id\", \"\");
        Identifier parsedId = Identifier.tryParse(entityId);
        EntityType<?> type = parsedId == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(parsedId);
        if (type != null) {
            label = type.getDescription();
        }
"""
    new = """        Component label = null;
        if (tag.get(\"CustomName\") != null && context.registries() != null) {
            label = ComponentSerialization.CODEC
                    .parse(context.registries().createSerializationContext(NbtOps.INSTANCE), tag.get(\"CustomName\"))
                    .result().orElse(null);
        }
        if (label == null) {
            String entityId = tag.getStringOr(\"id\", \"\");
            Identifier parsedId = Identifier.tryParse(entityId);
            EntityType<?> type = parsedId == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(parsedId);
            if (type != null) {
                label = type.getDescription();
            }
        }
"""
    if new in text:
        print("26.2 Knapsack tooltip parity pass changed 0 files")
    else:
        if old not in text:
            raise RuntimeError("Could not locate Knapsack tooltip label block")
        text = text.replace(old, new, 1)
        text = add_import(text, "net.minecraft.nbt.NbtOps")
        text = add_import(text, "net.minecraft.network.chat.ComponentSerialization")
        PATH.write_text(text, encoding="utf-8")
        print(f"26.2 Knapsack tooltip parity pass changed {PATH}")


if __name__ == "__main__":
    main()
    next_pass = Path(".github/port/structural_26_2_24.py")
    if next_pass.exists():
        runpy.run_path(str(next_pass), run_name="__main__")
