#!/usr/bin/env python3
"""Migrate Naturalist data-pack and sound JSON to Minecraft 26.2 formats.

This pass is intentionally conservative and idempotent:
- recipe ingredients: {"item": "id"} -> "id", {"tag": "id"} -> "#id"
- entity predicates: "type" -> "entity_type" where 26.2 expects an entity predicate
- old raw vanilla sound-file references -> stable vanilla sound-event references
"""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

ROOT = Path("common/src/main/resources/data")
SOUNDS = Path("common/src/main/resources/assets/naturalist/sounds.json")
changed: list[Path] = []


def normalize_ingredient(value: Any) -> Any:
    if isinstance(value, dict):
        if set(value) == {"item"} and isinstance(value["item"], str):
            return value["item"]
        if set(value) == {"tag"} and isinstance(value["tag"], str):
            return "#" + value["tag"].lstrip("#")
        return {key: normalize_ingredient(child) for key, child in value.items()}
    if isinstance(value, list):
        return [normalize_ingredient(child) for child in value]
    return value


def migrate_recipe(data: Any) -> Any:
    if not isinstance(data, dict):
        return data

    recipe_type = data.get("type")
    if recipe_type == "minecraft:crafting_shaped" and isinstance(data.get("key"), dict):
        data["key"] = {key: normalize_ingredient(value) for key, value in data["key"].items()}
    elif recipe_type == "minecraft:crafting_shapeless" and isinstance(data.get("ingredients"), list):
        data["ingredients"] = [normalize_ingredient(value) for value in data["ingredients"]]
    elif recipe_type in {
        "minecraft:smelting",
        "minecraft:blasting",
        "minecraft:smoking",
        "minecraft:campfire_cooking",
        "minecraft:stonecutting",
    } and "ingredient" in data:
        data["ingredient"] = normalize_ingredient(data["ingredient"])
    elif recipe_type in {"minecraft:smithing_transform", "minecraft:smithing_trim"}:
        for field in ("template", "base", "addition"):
            if field in data:
                data[field] = normalize_ingredient(data[field])

    return data


def migrate_entity_predicate(predicate: Any) -> Any:
    if isinstance(predicate, list):
        return [migrate_entity_predicate(value) for value in predicate]
    if not isinstance(predicate, dict):
        return predicate

    if "type" in predicate and "entity_type" not in predicate and isinstance(predicate["type"], str):
        predicate["entity_type"] = predicate.pop("type")

    for field in ("entity", "vehicle", "passenger", "targeted_entity", "source_entity", "direct_entity"):
        if field in predicate:
            predicate[field] = migrate_entity_predicate(predicate[field])

    return predicate


def migrate_predicates(node: Any) -> Any:
    if isinstance(node, list):
        return [migrate_predicates(value) for value in node]
    if not isinstance(node, dict):
        return node

    condition = node.get("condition")
    if condition == "minecraft:entity_properties" and "predicate" in node:
        node["predicate"] = migrate_entity_predicate(node["predicate"])
    elif condition == "minecraft:damage_source_properties" and isinstance(node.get("predicate"), dict):
        damage = node["predicate"]
        for field in ("source_entity", "direct_entity"):
            if field in damage:
                damage[field] = migrate_entity_predicate(damage[field])

    trigger = node.get("trigger")
    if isinstance(trigger, str) and isinstance(node.get("conditions"), dict):
        conditions = node["conditions"]
        for field in ("entity", "vehicle", "passenger", "targeted_entity"):
            if field in conditions:
                conditions[field] = migrate_entity_predicate(conditions[field])

    for key, value in list(node.items()):
        node[key] = migrate_predicates(value)
    return node


def migrate_sounds(data: Any) -> Any:
    if not isinstance(data, dict):
        return data

    # These Naturalist events used raw internal vanilla OGG paths that no longer resolve in
    # Minecraft 26.2. Referencing the registered vanilla events is both stable and equivalent.
    for event in ("entity.blobfish.flop", "entity.piranha.flop", "entity.catfish.flop"):
        entry = data.get(event)
        if isinstance(entry, dict):
            entry["sounds"] = [
                {
                    "name": "minecraft:entity.puffer_fish.flop",
                    "type": "event",
                    "volume": 0.3,
                }
            ]

    zebra = data.get("entity.zebra.eat")
    if isinstance(zebra, dict):
        zebra["sounds"] = [
            {
                "name": "minecraft:entity.horse.eat",
                "type": "event",
            }
        ]

    return data


def write_if_changed(path: Path, transform) -> None:
    try:
        original_text = path.read_text(encoding="utf-8")
        data = json.loads(original_text)
    except (OSError, json.JSONDecodeError):
        return

    transformed = transform(data)
    new_text = json.dumps(transformed, ensure_ascii=False, indent=2) + "\n"
    if new_text != original_text:
        path.write_text(new_text, encoding="utf-8")
        changed.append(path)


if ROOT.exists():
    for path in ROOT.rglob("*.json"):
        parts = set(path.parts)
        if "recipe" in parts or "recipes" in parts:
            write_if_changed(path, migrate_recipe)
        elif "advancement" in parts or "advancements" in parts or "loot_table" in parts or "loot_tables" in parts:
            write_if_changed(path, migrate_predicates)

if SOUNDS.exists():
    write_if_changed(SOUNDS, migrate_sounds)

print(f"26.2 data/resource migration changed {len(changed)} files")
for path in changed:
    print(path)
