from pathlib import Path
import re

ROOTS = [
    Path("common/src/main/java/com/crispytwig/naturalist"),
    Path("fabric/src/main/java/com/crispytwig/naturalist"),
]


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    imports = list(re.finditer(r"^import .+;\n", text, re.MULTILINE))
    if imports:
        i = imports[-1].end()
        return text[:i] + line + text[i:]
    return text


def patch_great_white(text: str) -> str:
    text = text.replace(
        "protected @NotNull AABB getAttackBoundingBox() {\n        return super.getAttackBoundingBox().inflate(0.9D, 0.5D, 0.9D);\n    }",
        "protected @NotNull AABB getAttackBoundingBox(double horizontalExpansion) {\n        return super.getAttackBoundingBox(horizontalExpansion).inflate(0.9D, 0.5D, 0.9D);\n    }",
    )
    return text


def patch_snake(text: str) -> str:
    if "public boolean isFood(@NotNull ItemStack stack)" not in text:
        marker = "    @Override\n    public boolean wantsToPickUp(@NotNull ServerLevel level, @NotNull ItemStack itemStack)"
        method = (
            "    @Override\n"
            "    public boolean isFood(@NotNull ItemStack stack) {\n"
            "        return FOOD_ITEMS.test(stack);\n"
            "    }\n\n"
        )
        if marker in text:
            text = text.replace(marker, method + marker)
    return text


def patch_snail(text: str) -> str:
    return text.replace("DyeColor dyeColor = stack.get(DataComponents.DYE);", "DyeColor dyeColor = itemStack.get(DataComponents.DYE);")


def patch_beached(text: str) -> str:
    return text.replace("mob.hasImpulse = true;", "mob.hurtMarked = true;")


def patch_recipes_registry(text: str) -> str:
    return text.replace(
        "DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> BUG_NET_SERIALIZER",
        "DeferredHolder<RecipeSerializer<?>, RecipeSerializer<BugNetInteractionRecipe>> BUG_NET_SERIALIZER",
    )


def patch_snail_shell_be(text: str) -> str:
    old = '''    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        this.saveAdditional(tag, registries);
        return tag;
    }'''
    new = '''    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        return this.saveCustomOnly(registries);
    }'''
    return text.replace(old, new)


def patch_clone_signature(text: str) -> str:
    text = text.replace(
        "public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state) {",
        "public @NotNull ItemStack getCloneItemStack(@NotNull LevelReader level, @NotNull BlockPos pos, @NotNull BlockState state, boolean includeData) {",
    )
    return text


def patch_tortoise(text: str) -> str:
    text = patch_clone_signature(text)
    text = text.replace("super.getCloneItemStack(level, pos, state, true)", "super.getCloneItemStack(level, pos, state, includeData)")
    return text


def patch_naturalist_bucket(text: str) -> str:
    text = add_import(text, "net.minecraft.world.entity.Mob")
    return text


def patch_caught_mob(text: str) -> str:
    text = add_import(text, "net.minecraft.world.entity.Mob")
    text = add_import(text, "net.minecraft.world.entity.LivingEntity")
    text = text.replace(
        "super(entitySupplier.get(), fluidSupplier.get(), soundSupplier.get(), properties, true, tooltipPrefix, variantNames);",
        "super((EntityType<? extends Mob>) entitySupplier.get(), fluidSupplier.get(), soundSupplier.get(), properties, true, tooltipPrefix, variantNames);",
    )
    text = text.replace(
        "public void checkExtraContent(@Nullable Player player, @NotNull Level level, @NotNull ItemStack containerStack, @NotNull BlockPos pos)",
        "public void checkExtraContent(@Nullable LivingEntity player, @NotNull Level level, @NotNull ItemStack containerStack, @NotNull BlockPos pos)",
    )
    return text


def patch_hedgehog(text: str) -> str:
    text = add_import(text, "net.minecraft.world.entity.LivingEntity")
    text = text.replace("    @Override\n    public int getEnchantmentValue()", "    public int getEnchantmentValue()")
    text = text.replace("    @Override\n    public boolean isEnchantable(@NotNull ItemStack stack)", "    public boolean isEnchantable(@NotNull ItemStack stack)")
    text = text.replace(
        "public void checkExtraContent(@Nullable Player player, @NotNull Level level, @NotNull ItemStack containerStack, @NotNull BlockPos pos)",
        "public void checkExtraContent(@Nullable LivingEntity player, @NotNull Level level, @NotNull ItemStack containerStack, @NotNull BlockPos pos)",
    )
    return text


def patch_knapsack(text: str) -> str:
    old_save = '''        Mob mob = (Mob) target;
        CompoundTag entityTag = new CompoundTag();
        mob.save(entityTag);'''
    new_save = '''        Mob mob = (Mob) target;
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mob.registryAccess());
        mob.save(output);
        CompoundTag entityTag = output.buildResult();'''
    text = text.replace(old_save, new_save)

    text = text.replace(
        "Entity entity = EntityType.loadEntityRecursive(tag, serverLevel, e -> {\n                e.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, e.getYRot(), e.getXRot());",
        "Entity entity = EntityType.loadEntityRecursive(tag, serverLevel, EntitySpawnReason.LOAD, e -> {\n                e.snapTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, e.getYRot(), e.getXRot());",
    )

    old_tooltip = '''        Component label = null;
        if (tag.contains("CustomName", 8) && context.registries() != null) {
            label = Component.Serializer.fromJson(tag.getString("CustomName"), context.registries());
        }
        if (label == null) {
            label = EntityType.byString(tag.getString("id")).map(EntityType::getDescription).orElse(null);
        }'''
    new_tooltip = '''        Component label = null;
        String entityId = tag.getStringOr("id", "");
        Identifier parsedId = Identifier.tryParse(entityId);
        EntityType<?> type = parsedId == null ? null : BuiltInRegistries.ENTITY_TYPE.getValue(parsedId);
        if (type != null) {
            label = type.getDescription();
        }'''
    text = text.replace(old_tooltip, new_tooltip)
    text = add_import(text, "net.minecraft.core.registries.BuiltInRegistries")
    text = add_import(text, "net.minecraft.util.ProblemReporter")
    text = add_import(text, "net.minecraft.world.entity.EntitySpawnReason")
    text = add_import(text, "net.minecraft.world.level.storage.TagValueOutput")
    return text


def patch_snail_item(text: str) -> str:
    text = text.replace(
        "public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)",
        "public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag)",
    )
    text = text.replace("tooltip.add(", "tooltip.accept(")
    if "TooltipDisplay display" in text:
        text = add_import(text, "net.minecraft.world.item.component.TooltipDisplay")
    return text


def patch_bug_net_item(text: str) -> str:
    old = '''        Optional<RecipeHolder<BugNetInteractionRecipe>> allRecipes = player.level().getRecipeManager().getAllRecipesFor(NaturalistRecipes.BUG_NET.get())
                .stream()
                .filter(r -> r.value().entityType() == interactionTarget.getType())
                .findFirst();

        if (allRecipes.isPresent()) {
            var dropItem = allRecipes.get().value().dropStack().copy();'''
    new = '''        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        Optional<BugNetInteractionRecipe> allRecipes = serverLevel.recipeAccess().getRecipes()
                .stream()
                .map(RecipeHolder::value)
                .filter(BugNetInteractionRecipe.class::isInstance)
                .map(BugNetInteractionRecipe.class::cast)
                .filter(r -> r.entityType() == interactionTarget.getType())
                .findFirst();

        if (allRecipes.isPresent()) {
            var dropItem = allRecipes.get().dropStack().copy();'''
    text = text.replace(old, new)
    if "ServerLevel serverLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    return text


def patch_glow_goop_item(text: str) -> str:
    text = text.replace(
        "public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn)",
        "public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flagIn)",
    )
    if "TooltipDisplay display" in text:
        text = add_import(text, "net.minecraft.world.item.component.TooltipDisplay")
    return text


def patch_block_entities(text: str) -> str:
    text = add_import(text, "net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder")
    text = text.replace(
        "BlockEntityType.Builder.of(AntHillBlockEntity::new, NaturalistRegistry.ANT_HILL.get()).build(null)",
        "FabricBlockEntityTypeBuilder.create(AntHillBlockEntity::new, NaturalistRegistry.ANT_HILL.get()).build()",
    )
    text = text.replace(
        "BlockEntityType.Builder.of(SnailShellBlockEntity::new, NaturalistRegistry.SNAIL_SHELL_BLOCK.get()).build(null)",
        "FabricBlockEntityTypeBuilder.create(SnailShellBlockEntity::new, NaturalistRegistry.SNAIL_SHELL_BLOCK.get()).build()",
    )
    return text


def patch_brewing(text: str) -> str:
    text = text.replace(
        "builder.registerPotionRecipe(from, ingredient, to)",
        "builder.registerPotionRecipe(from, Ingredient.of(ingredient), to)",
    )
    if "Ingredient.of(ingredient)" in text:
        text = add_import(text, "net.minecraft.world.item.crafting.Ingredient")
    return text


def patch_file(path: Path, text: str) -> str:
    name = path.name
    if name == "GreatWhiteShark.java":
        return patch_great_white(text)
    if name == "Snake.java":
        return patch_snake(text)
    if name == "Snail.java":
        return patch_snail(text)
    if name == "BeachedMob.java":
        return patch_beached(text)
    if name == "NaturalistRecipes.java":
        return patch_recipes_registry(text)
    if name == "SnailShellBlockEntity.java":
        return patch_snail_shell_be(text)
    if name == "GlowGoopBlock.java":
        return patch_clone_signature(text)
    if name == "TortoiseEggBlock.java":
        return patch_tortoise(text)
    if name == "NaturalistBucketItem.java":
        return patch_naturalist_bucket(text)
    if name == "CaughtMobItem.java":
        return patch_caught_mob(text)
    if name == "HedgehogItem.java":
        return patch_hedgehog(text)
    if name == "KnapsackItem.java":
        return patch_knapsack(text)
    if name == "SnailItem.java":
        return patch_snail_item(text)
    if name == "BugNetItem.java":
        return patch_bug_net_item(text)
    if name == "GlowGoopItem.java":
        return patch_glow_goop_item(text)
    if name == "NaturalistBlockEntities.java":
        return patch_block_entities(text)
    if name == "NaturalistFabric.java":
        return patch_brewing(text)
    return text


def main() -> None:
    changed = []
    for root in ROOTS:
        if not root.exists():
            continue
        for path in root.rglob("*.java"):
            old = path.read_text(encoding="utf-8")
            new = patch_file(path, old)
            if new != old:
                path.write_text(new, encoding="utf-8")
                changed.append(str(path))
    print(f"26.2 structural pass 7 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
