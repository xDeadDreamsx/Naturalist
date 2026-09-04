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

def patch_common(text: str) -> str:
    text = text.replace("this.input.jumping", "this.input.jump()")
    text = text.replace(".getParamOrNull(", ".getOptionalParameter(")
    text = text.replace("EntitySpawnReason.SPAWN_EGG", "EntitySpawnReason.SPAWN_ITEM_USE")
    text = text.replace(".getUnsafe()", ".copyTag()")
    text = text.replace("Screen.hasShiftDown()", "Minecraft.getInstance().hasShiftDown()")
    text = text.replace("BuiltInRegistries.REGISTRY.get(registryKey.identifier())", "BuiltInRegistries.REGISTRY.getValue(registryKey.identifier())")
    text = text.replace("BuiltInRegistries.ENTITY_TYPE.get(buf.readIdentifier())", "BuiltInRegistries.ENTITY_TYPE.getValue(buf.readIdentifier())")
    text = text.replace("level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));", "ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));")
    text = text.replace("tag.contains(\"Color\", Tag.TAG_ANY_NUMERIC)", "tag.contains(\"Color\")")
    text = text.replace("DyeColor.byId(tag.getInt(\"Color\"))", "DyeColor.byId(tag.getIntOr(\"Color\", 0))")
    return text

def patch_crab(text: str) -> str:
    return text.replace(
        'ItemStack held = tag.get("HeldItem").flatMap(encoded -> ItemStack.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), encoded).result()).orElse(ItemStack.EMPTY);',
        'ItemStack held = tag.get("HeldItem") != null ? ItemStack.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag.get("HeldItem")).result().orElse(ItemStack.EMPTY) : ItemStack.EMPTY;'
    )

def patch_great_white(text: str) -> str:
    return text.replace(
        "protected AABB getAttackBoundingBox() {\n        return super.getAttackBoundingBox().inflate(0.9D, 0.5D, 0.9D);\n    }",
        "protected AABB getAttackBoundingBox(double horizontalExpansion) {\n        return super.getAttackBoundingBox(horizontalExpansion).inflate(0.9D, 0.5D, 0.9D);\n    }"
    )

def patch_snake(text: str) -> str:
    text = text.replace("this.isTameFood(stack)", "TAME_ITEMS.test(stack)")
    if "public boolean isFood(@NotNull ItemStack stack)" not in text:
        method = (
            "    @Override\n"
            "    public boolean isFood(@NotNull ItemStack stack) {\n"
            "        return FOOD_ITEMS.test(stack);\n"
            "    }\n\n"
        )
        insert = text.find("    @Override\n    protected void registerGoals()")
        if insert >= 0:
            text = text[:insert] + method + text[insert:]
    return text

def patch_giant_isopod(text: str) -> str:
    old = '''        return !this.level().getNearbyPlayers(
                TargetingConditions.forNonCombat().range(3.0D).selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)),
                this, this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D)).isEmpty();'''
    new = '''        return !this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR::test).isEmpty();'''
    return text.replace(old, new)

def patch_snail(text: str) -> str:
    text = text.replace("DyeColor dyeColor = dyeItem.getDyeColor();", "DyeColor dyeColor = stack.get(DataComponents.DYE);")
    if "DataComponents.DYE" in text:
        text = add_import(text, "net.minecraft.core.component.DataComponents")
    return text

def patch_camera_models(text: str) -> str:
    return text.replace("Minecraft.getInstance().gameRenderer.getMainCamera().getPosition()", "Minecraft.getInstance().getEntityRenderDispatcher().camera.position()")

def patch_hedgehog_model(text: str) -> str:
    text = text.replace("this.animate(entity.hideAnimationState,", "this.animateUnblended(entity.hideAnimationState,")
    text = text.replace("this.animate(entity.unhideAnimationState,", "this.animateUnblended(entity.unhideAnimationState,")
    return text

def patch_stale_root_override(text: str) -> str:
    text = re.sub(r"\n@Override\n(\s*protected String getRootPartName\(\))", r"\n\1", text)
    text = re.sub(r"\n\t@Override\n(\tprotected String getRootPartName\(\))", r"\n\1", text)
    text = re.sub(r"\n    @Override\n(\s*protected String getRootPartName\(\))", r"\n\1", text)
    return text

def patch_dirt_trail(text: str) -> str:
    if "hurtServer(ServerLevel" not in text:
        insert = text.rfind("\n}")
        method = '''\n    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }\n'''
        if insert >= 0:
            text = text[:insert] + method + text[insert:]
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
        text = add_import(text, "net.minecraft.world.damagesource.DamageSource")
    return text

def patch_parrot_flight(text: str) -> str:
    text = text.replace("return isParrot(player.getShoulderEntityLeft()) && isParrot(player.getShoulderEntityRight());", "return player.getShoulderParrotLeft().isPresent() && player.getShoulderParrotRight().isPresent();")
    text = re.sub(r"\n    private static boolean isParrot\(CompoundTag tag\) \{\n        return .*?\n    \}\n", "\n", text, flags=re.DOTALL)
    text = text.replace("import net.minecraft.nbt.CompoundTag;\n", "")
    text = text.replace("import net.minecraft.world.entity.EntityType;\n", "")
    text = text.replace("import net.minecraft.world.entity.EntityTypes;\n", "")
    return text

def patch_thrown_duck_egg(text: str) -> str:
    if "new ItemStack(" in text:
        text = add_import(text, "net.minecraft.world.item.ItemStack")
    if "EntitySpawnReason." in text:
        text = add_import(text, "net.minecraft.world.entity.EntitySpawnReason")
    return text

def patch_variant_util(text: str) -> str:
    text = text.replace("level.registryAccess().registry(registryKey)", "level.registryAccess().lookup(registryKey)")
    text = text.replace("registry.holders()", "registry.listElements()")
    return text

def patch_pet_targeting(text: str) -> str:
    old = '''        return !self.isTame() || self.getOwnerUUID() == null || !(target instanceof OwnableEntity ownable)
                || !self.getOwnerUUID().equals(ownable.getOwnerUUID());'''
    new = '''        if (!self.isTame()) {
            return true;
        }
        LivingEntity owner = self.getOwner();
        return owner == null || !(target instanceof OwnableEntity ownable) || ownable.getOwner() != owner;'''
    text = text.replace(old, new)
    if "LivingEntity owner =" in text:
        text = add_import(text, "net.minecraft.world.entity.LivingEntity")
    return text

def patch_bugnet_recipe(text: str) -> str:
    start = text.find("public record BugNetInteractionRecipe")
    ser = text.find("    public static final class Serializer")
    if start < 0 or ser < 0:
        return text
    header_end = text.find("{", start)
    prefix = text[:header_end+1]
    suffix = text[ser:]
    body = '''

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput input) {
        return dropStack.copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return NaturalistRecipes.BUG_NET_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<? extends Recipe<RecipeInput>> getType() {
        return NaturalistRecipes.BUG_NET.get();
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

'''
    text = prefix + body + suffix
    text = add_import(text, "net.minecraft.world.item.crafting.PlacementInfo")
    text = add_import(text, "net.minecraft.world.item.crafting.RecipeBookCategory")
    text = add_import(text, "net.minecraft.world.item.crafting.RecipeBookCategories")
    text = text.replace("EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(buf.readIdentifier());", "EntityType<?> entityType = java.util.Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getValue(buf.readIdentifier()));")
    return text

def patch_antivenom(text: str) -> str:
    text = text.replace("public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity indirectSource, @NotNull LivingEntity target, int amplifier, double health) {", "public void applyInstantaneousEffect(@NotNull ServerLevel level, @Nullable Entity source, @Nullable Entity indirectSource, @NotNull LivingEntity target, int amplifier, double health) {")
    if "applyInstantaneousEffect(@NotNull ServerLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    return text

def patch_snail_shell_be(text: str) -> str:
    text = re.sub(r'''    @Override
    protected void saveAdditional\(.*?\n    \}

    @Override
    protected void loadAdditional\(.*?\n    \}''', '''    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (!this.flower.isEmpty()) {
            output.store("Flower", ItemStack.CODEC, this.flower);
        }
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.flower = input.read("Flower", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }''', text, count=1, flags=re.DOTALL)
    if "ValueOutput output" in text:
        text = add_import(text, "net.minecraft.world.level.storage.ValueOutput")
        text = add_import(text, "net.minecraft.world.level.storage.ValueInput")
    return text

def patch_starfish_block(text: str) -> str:
    text = re.sub(r"(\n\s*)@Override(\n\s*public @NotNull MultifaceSpreader getSpreader\(\))", r"\1\2", text)
    text = text.replace("level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));", "ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));")
    return text

def patch_egg_blocks(text: str) -> str:
    return re.sub(r"public void fallOn\((@NotNull )?Level level, (@NotNull )?BlockState state, (@NotNull )?BlockPos pos, (@NotNull )?Entity entity, float fallDistance\)", r"public void fallOn(\1Level level, \2BlockState state, \3BlockPos pos, \4Entity entity, double fallDistance)", text)

def patch_ant_hill(text: str) -> str:
    old = '''    @Override
    protected void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AntHillBlockEntity hill) {
            Containers.dropContents(level, pos, hill.getStorage());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }'''
    new = '''    @Override
    protected void affectNeighborsAfterRemoval(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof AntHillBlockEntity hill) {
            Containers.dropContents(level, pos, hill.getStorage());
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }'''
    return text.replace(old, new)

def patch_bucket_item(text: str) -> str:
    text = text.replace("private final EntityType<?> variantEntityType;", "private final EntityType<? extends Mob> variantEntityType;")
    text = text.replace("NaturalistBucketItem(EntityType<?> entityType,", "NaturalistBucketItem(EntityType<? extends Mob> entityType,")
    text = text.replace("player.setItemInHand(context.getHand(), release(level, player, context.getItemInHand(), placePos).getObject());\n        return InteractionResult.SUCCESS;", "return release(level, player, context.getItemInHand(), placePos);")
    text = text.replace("public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)", "public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag)")
    text = text.replace("tooltip.add(", "tooltip.accept(")
    if "TooltipDisplay display" in text:
        text = add_import(text, "net.minecraft.world.item.component.TooltipDisplay")
    return text

def patch_tooltip_items(text: str) -> str:
    text = text.replace("public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)", "public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag)")
    text = text.replace("public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag)", "public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag)")
    text = text.replace("tooltip.add(", "tooltip.accept(")
    if "TooltipDisplay display" in text:
        text = add_import(text, "net.minecraft.world.item.component.TooltipDisplay")
    return text

def patch_glow_goop_item(text: str) -> str:
    text = patch_tooltip_items(text)
    if "Minecraft.getInstance()" in text:
        text = add_import(text, "net.minecraft.client.Minecraft")
    return text

def patch_entity_types(text: str) -> str:
    text = text.replace("builder.build(Naturalist.location(id).toString())", "builder.build(ResourceKey.create(Registries.ENTITY_TYPE, Naturalist.location(id)))")
    if "ResourceKey.create(Registries.ENTITY_TYPE" in text:
        text = add_import(text, "net.minecraft.resources.ResourceKey")
        text = add_import(text, "net.minecraft.core.registries.Registries")
    return text

def patch_registry_food(text: str) -> str:
    old = "new FoodProperties.Builder().nutrition(2).saturationModifier(0.8F).effect(new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F).build()"
    new = "new FoodProperties.Builder().nutrition(2).saturationModifier(0.8F).build(), Consumables.defaultFood().onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F)).build()"
    text = text.replace(old, new)
    if "ApplyStatusEffectsConsumeEffect" in text:
        text = add_import(text, "net.minecraft.world.item.component.Consumables")
        text = add_import(text, "net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect")
    return text

def patch_spawn_registration(text: str) -> str:
    text = text.replace("Snake::checkSnakeSpawnRules", "Animal::checkAnimalSpawnRules")
    if "Animal::checkAnimalSpawnRules" in text:
        text = add_import(text, "net.minecraft.world.entity.animal.Animal")
    return text

def patch_spawn_egg(text: str) -> str:
    return text.replace("return new SpawnEggItem(type.get(), primaryColor, secondaryColor, properties);", "return new SpawnEggItem(properties.spawnEgg(type.get()));")

def patch_brewing(text: str) -> str:
    return text.replace("Naturalist.registerPotionMixes(builder::registerItemRecipe)", "Naturalist.registerPotionMixes((from, ingredient, to) -> builder.registerPotionRecipe(from, ingredient, to))")

def patch_file(path: Path, text: str) -> str:
    name = path.name
    text = patch_common(text)
    if name == "Crab.java": text = patch_crab(text)
    elif name == "GreatWhiteShark.java": text = patch_great_white(text)
    elif name == "Snake.java": text = patch_snake(text)
    elif name == "GiantIsopod.java": text = patch_giant_isopod(text)
    elif name == "Snail.java": text = patch_snail(text)
    elif name in {"CrabModel.java", "SnailModel.java"}: text = patch_camera_models(text)
    elif name == "HedgehogModel.java": text = patch_hedgehog_model(text)
    elif name in {"VultureModel.java", "DirtTrailModel.java", "SnakeModel.java"}: text = patch_stale_root_override(text)
    elif name == "DirtTrail.java": text = patch_dirt_trail(text)
    elif name == "ParrotFlight.java": text = patch_parrot_flight(text)
    elif name == "ThrownDuckEgg.java": text = patch_thrown_duck_egg(text)
    elif name == "MobVariantUtil.java": text = patch_variant_util(text)
    elif name == "PetTargeting.java": text = patch_pet_targeting(text)
    elif name == "BugNetInteractionRecipe.java": text = patch_bugnet_recipe(text)
    elif name == "AntivenomMobEffect.java": text = patch_antivenom(text)
    elif name == "SnailShellBlockEntity.java": text = patch_snail_shell_be(text)
    elif name == "StarfishBlock.java": text = patch_starfish_block(text)
    elif name in {"OstrichEggBlock.java", "TortoiseEggBlock.java", "AlligatorEggBlock.java"}: text = patch_egg_blocks(text)
    elif name == "AntHillBlock.java": text = patch_ant_hill(text)
    elif name == "NaturalistBucketItem.java": text = patch_bucket_item(text)
    elif name in {"CaughtMobItem.java", "HedgehogItem.java", "KnapsackItem.java"}: text = patch_tooltip_items(text)
    elif name == "GlowGoopItem.java": text = patch_glow_goop_item(text)
    elif name == "NaturalistEntityTypes.java": text = patch_entity_types(text)
    elif name == "NaturalistRegistry.java": text = patch_registry_food(text)
    elif name == "Naturalist.java": text = patch_spawn_registration(text)
    elif name == "FabricRegistryHelper.java": text = patch_spawn_egg(text)
    elif name == "NaturalistFabric.java": text = patch_brewing(text)
    return text

def main() -> None:
    changed = []
    for root in ROOTS:
        if not root.exists(): continue
        for path in root.rglob("*.java"):
            old = path.read_text(encoding="utf-8")
            new = patch_file(path, old)
            if new != old:
                path.write_text(new, encoding="utf-8")
                changed.append(str(path))
    print(f"26.2 structural pass 6 changed {len(changed)} files")
    for path in changed: print(path)

if __name__ == "__main__":
    main()
