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
    if qualified.startswith("net.minecraft.world.entity.") and "import net.minecraft.world.entity.*;\n" in text:
        return text
    imports = list(re.finditer(r"^import .+;\n", text, re.MULTILINE))
    if imports:
        i = imports[-1].end()
        return text[:i] + line + text[i:]
    return text


def common(text: str) -> str:
    text = text.replace("SoundEvents.SALMON_FLOP.value()", "SoundEvents.SALMON_FLOP")
    text = text.replace(".noCollission()", ".noCollision()")
    text = text.replace("MobEffects.MOVEMENT_SPEED", "MobEffects.SPEED")

    # Spawned entities no longer expose moveTo; snapTo is the direct positioning replacement.
    text = re.sub(
        r"\b(baby|snail|ant|alligator|butterfly|duck|hedgehog|zoglin)\.moveTo\(",
        r"\1.snapTo(",
        text,
    )

    # Item particles can directly carry a plain Item when stack components are irrelevant.
    text = re.sub(
        r"new ItemParticleOption\(ParticleTypes\.ITEM, new ItemStack\(([^()]+(?:\([^()]*\))?)\)\)",
        r"new ItemParticleOption(ParticleTypes.ITEM, \1)",
        text,
    )

    # Entity experience hook gained ServerLevel.
    text = text.replace(
        "public int getBaseExperienceReward() {",
        "protected int getBaseExperienceReward(ServerLevel level) {",
    )
    if "getBaseExperienceReward(ServerLevel level)" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")

    if "EntitySpawnReason." in text:
        text = add_import(text, "net.minecraft.world.entity.EntitySpawnReason")
    return text


def patch_simple_knockback(text: str) -> str:
    old_decl = "public void knockback(double strength, double x, double z) {"
    if old_decl not in text:
        return text
    text = text.replace(
        old_decl,
        "public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {",
        1,
    )
    # Limit replacements to the first knockback method body.
    start = text.find("public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {")
    if start >= 0:
        end = text.find("\n    }", start)
        if end >= 0:
            body = text[start:end]
            body = re.sub(
                r"super\.knockback\((.*?),\s*x,\s*z\);",
                r"super.knockback(\1, x, z, source, sourceStrength);",
                body,
            )
            text = text[:start] + body + text[end:]
    text = add_import(text, "net.minecraft.world.damagesource.DamageSource")
    return text


def patch_vulture(text: str) -> str:
    text = text.replace(
        "public boolean isInvulnerableTo(DamageSource source) {\n"
        "        return source.equals(this.damageSources().cactus()) || super.isInvulnerableTo(source);\n"
        "    }",
        "public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {\n"
        "        return source.equals(this.damageSources().cactus()) || super.isInvulnerableTo(level, source);\n"
        "    }",
    )
    text = text.replace(
        "public boolean canTakeItem(@NotNull ItemStack itemStack) {",
        "public boolean wantsToPickUp(@NotNull ServerLevel level, @NotNull ItemStack itemStack) {",
    )
    text = text.replace(
        "protected void pickUpItem(ItemEntity itemEntity) {",
        "protected void pickUpItem(ServerLevel level, ItemEntity itemEntity) {",
    )
    return add_import(text, "net.minecraft.server.level.ServerLevel")


def patch_great_white(text: str) -> str:
    text = text.replace(
        "protected AABB getAttackBoundingBox() {\n"
        "        return super.getAttackBoundingBox().inflate(0.9D, 0.5D, 0.9D);\n"
        "    }",
        "protected AABB getAttackBoundingBox(double horizontalExpansion) {\n"
        "        return super.getAttackBoundingBox(horizontalExpansion).inflate(0.9D, 0.5D, 0.9D);\n"
        "    }",
    )
    text = text.replace(
        "this.shark.doHurtTarget(target);",
        "if (this.shark.level() instanceof ServerLevel serverLevel) { this.shark.doHurtTarget(serverLevel, target); }",
    )
    if "ServerLevel serverLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    return text


def patch_rhino(text: str) -> str:
    text = patch_simple_knockback(text)
    text = text.replace(
        "    @Override\n    protected void blockedByShield(LivingEntity defender) {",
        "    protected void blockedByShield(LivingEntity defender) {",
    )
    text = text.replace(
        "if (this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {",
        "if (this.mob.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {",
    )
    text = text.replace(
        "List<LivingEntity> nearbyEntities = this.mob.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(), this.mob, this.mob.getBoundingBox());",
        "List<LivingEntity> nearbyEntities = this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox(), entity -> entity != this.mob);",
    )
    old = """livingEntity.hurt(livingEntity.damageSources().mobAttack(this.mob), (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    float speed = Mth.clamp(this.mob.getSpeed() * 1.65f, 0.2f, 3.0f);
                    float shieldBlockModifier = livingEntity.isDamageSourceBlocked(livingEntity.damageSources().mobAttack(this.mob)) ? 0.5f : 1.0f;
                    livingEntity.knockback(shieldBlockModifier * speed * 2.0D, this.chargeDirection.x(), this.chargeDirection.z());"""
    new = """DamageSource attackSource = livingEntity.damageSources().mobAttack(this.mob);
                    if (this.mob.level() instanceof ServerLevel serverLevel) {
                        livingEntity.hurtServer(serverLevel, attackSource, (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    }
                    float speed = Mth.clamp(this.mob.getSpeed() * 1.65f, 0.2f, 3.0f);
                    float shieldBlockModifier = livingEntity.getItemBlockingWith() != null ? 0.5f : 1.0f;
                    livingEntity.knockback(shieldBlockModifier * speed * 2.0D, this.chargeDirection.x(), this.chargeDirection.z(), attackSource, 0.0F);"""
    text = text.replace(old, new)
    text = text.replace(
        "super(mob, Player.class, 10, true, true, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);",
        "super(mob, Player.class, 10, true, true, (entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));",
    )
    return add_import(text, "net.minecraft.server.level.ServerLevel")


def patch_neutral_mob(text: str, class_name: str) -> str:
    # Convert the legacy relative anger timer + UUID target to 26.2's absolute time + EntityReference.
    text = re.sub(
        r"private int remainingPersistentAngerTime;\s*@Nullable\s*private UUID persistentAngerTarget;",
        "private long persistentAngerEndTime = -1L;\n    @Nullable\n    private EntityReference<LivingEntity> persistentAngerTarget;",
        text,
    )
    text = re.sub(
        r"@Nullable\s*private UUID persistentAngerTarget;",
        "@Nullable\n    private EntityReference<LivingEntity> persistentAngerTarget;\n    private long persistentAngerEndTime = -1L;",
        text,
    )
    legacy = re.compile(
        r"""    @Override
    public void setRemainingPersistentAngerTime\(int time\) \{
.*?
    @Override
    public void startPersistentAngerTimer\(\) \{
        this\.setRemainingPersistentAngerTime\(PERSISTENT_ANGER_TIME\.sample\(this\.random\)\);
    \}""",
        re.DOTALL,
    )
    replacement = """    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }"""
    text = legacy.sub(replacement, text, count=1)
    # Snake's anger timer is stored in synced data rather than a normal field.
    snake_legacy = re.compile(
        r"""    @Override
    public void setRemainingPersistentAngerTime\(int time\) \{
        this\.entityData\.set\(REMAINING_ANGER_TIME, time\);
    \}

    @Override
    public int getRemainingPersistentAngerTime\(\) \{
        return this\.entityData\.get\(REMAINING_ANGER_TIME\);
    \}

    @Override
    public void setPersistentAngerTarget\(@Nullable UUID target\) \{
        this\.persistentAngerTarget = target;
    \}

    @Nullable
    @Override
    public UUID getPersistentAngerTarget\(\) \{
        return this\.persistentAngerTarget;
    \}""",
        re.DOTALL,
    )
    snake_repl = """    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> target) {
        this.persistentAngerTarget = target;
    }

    @Nullable
    @Override
    public EntityReference<LivingEntity> getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME.sample(this.random));
    }"""
    text = snake_legacy.sub(snake_repl, text, count=1)
    if class_name == "Snake.java" and "private long persistentAngerEndTime" not in text:
        marker = "private boolean followingOwner = true;"
        text = text.replace(marker, marker + "\n    private long persistentAngerEndTime = -1L;")
    text = text.replace(
        "this.setPersistentAngerTarget(culprit.getUUID());",
        "this.setPersistentAngerTarget(EntityReference.of(culprit));",
    )
    text = add_import(text, "net.minecraft.world.entity.EntityReference")
    return text


def patch_ostrich(text: str) -> str:
    text = patch_neutral_mob(text, "Ostrich.java")
    old_save = """        long[] eggs = new long[this.ownedEggs.size()];
        int i = 0;
        for (BlockPos pos : this.ownedEggs) {
            eggs[i++] = pos.asLong();
        }
        compound.putLongArray("OwnedEggs", eggs);"""
    new_save = """        ValueOutput.TypedOutputList<BlockPos> eggs = compound.list("OwnedEggs", BlockPos.CODEC);
        for (BlockPos pos : this.ownedEggs) {
            eggs.add(pos);
        }"""
    text = text.replace(old_save, new_save)
    text = text.replace(
        'for (long packed : compound.getLongArray("OwnedEggs")) {\n'
        "            this.ownedEggs.add(BlockPos.of(packed));\n"
        "        }",
        'for (BlockPos pos : compound.listOrEmpty("OwnedEggs", BlockPos.CODEC)) {\n'
        "            this.ownedEggs.add(pos.immutable());\n"
        "        }",
    )
    text = re.sub(
        r"List<Player> players = this\.level\(\)\.getNearbyPlayers\(TargetingConditions\.forNonCombat\(\)\.range\(16\.0D\).*?;",
        "List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(16.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);",
        text,
        flags=re.DOTALL,
    )
    return text


def patch_snake(text: str) -> str:
    text = patch_neutral_mob(text, "Snake.java")
    text = re.sub(
        r"""    @Override
    public boolean canTakeItem\(@NotNull ItemStack itemStack\) \{
.*?
    \}

    @Override
    protected void pickUpItem\(@NotNull ItemEntity itemEntity\) \{""",
        """    @Override
    public boolean wantsToPickUp(@NotNull ServerLevel level, @NotNull ItemStack itemStack) {
        EquipmentSlot slot = getEquipmentSlotForItem(itemStack);
        return slot == EquipmentSlot.MAINHAND && this.getItemBySlot(slot).isEmpty() && FOOD_ITEMS.test(itemStack);
    }

    @Override
    protected void pickUpItem(@NotNull ServerLevel level, @NotNull ItemEntity itemEntity) {""",
        text,
        count=1,
        flags=re.DOTALL,
    )
    text = text.replace(
        "this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;",
        "this.setGuaranteedDrop(EquipmentSlot.MAINHAND);",
    )
    text = re.sub(
        r"""    @Override
    public boolean canSleep\(\) \{
        long dayTime = this\.level\(\)\.getDayTime\(\);
        if \(this\.isAngry\(\) \|\| this\.level\(\)\.isWaterAt\(this\.blockPosition\(\)\)\) \{
            return false;
        \} else if \(dayTime > 18000 && dayTime < 23000\) \{
            return false;
        \} else return dayTime > 12000 && dayTime < 28000;
    \}""",
        """    @Override
    public boolean canSleep() {
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        }
        return !this.level().isBrightOutside();
    }""",
        text,
    )
    text = re.sub(
        r"List<Player> players = this\.level\(\)\.getNearbyPlayers\(TargetingConditions\.forNonCombat\(\)\.range\(4\.0D\), this, this\.getBoundingBox\(\)\.inflate\(4\.0D, 2\.0D, 4\.0D\)\);",
        "List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);",
        text,
    )
    return text


def patch_hippo(text: str) -> str:
    text = patch_simple_knockback(text)
    text = text.replace(
        "this.mob.doHurtTarget(enemy);",
        "if (this.mob.level() instanceof ServerLevel serverLevel) { this.mob.doHurtTarget(serverLevel, enemy); }",
    )
    if "ServerLevel serverLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    return text


def patch_giraffe(text: str) -> str:
    text = text.replace(
        """        if (this.isControlledByLocalInstance()) {
            this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
            super.travel(new Vec3(f, travelVector.y, g));
        } else if (livingEntity instanceof Player) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.calculateEntityAnimation(false);
        this.tryCheckInsideBlocks();""",
        """        this.setSpeed((float)this.getAttributeValue(Attributes.MOVEMENT_SPEED));
        super.travel(new Vec3(f, travelVector.y, g));
        this.calculateEntityAnimation(false);""",
    )
    return text


def patch_duck(text: str) -> str:
    text = text.replace(
        "this.spawnAtLocation(NaturalistRegistry.DUCK_EGG.get());",
        "if (this.level() instanceof ServerLevel serverLevel) { this.spawnAtLocation(serverLevel, NaturalistRegistry.DUCK_EGG.get()); }",
    )
    return add_import(text, "net.minecraft.server.level.ServerLevel")


def patch_blobfish(text: str) -> str:
    return text.replace(
        "new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(NaturalistRegistry.BLOBFISH.get()))",
        "new ItemParticleOption(ParticleTypes.ITEM, NaturalistRegistry.BLOBFISH.get())",
    )


def patch_giant_isopod(text: str) -> str:
    text = patch_simple_knockback(text)
    text = re.sub(
        r"this\.level\(\)\.getNearbyPlayers\((?:.|\n)*?this\.getBoundingBox\(\)\)",
        "this.level().getEntitiesOfClass(Player.class, this.getBoundingBox(), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test)",
        text,
        count=1,
    )
    return text


def patch_surface_climbing(text: str) -> str:
    text = text.replace("EntityDataAccessor<Vector3f>", "EntityDataAccessor<Vector3fc>")
    text = text.replace("Vector3f current =", "Vector3fc current =")
    text = add_import(text, "org.joml.Vector3fc")
    text = add_import(text, "net.minecraft.world.level.storage.ValueInput")
    text = add_import(text, "net.minecraft.world.level.storage.ValueOutput")
    text = text.replace(
        'new Vec3(tag.getFloat("ClimbNormalX"), tag.getFloat("ClimbNormalY"), tag.getFloat("ClimbNormalZ"))',
        'new Vec3(tag.getFloatOr("ClimbNormalX", 0.0F), tag.getFloatOr("ClimbNormalY", 1.0F), tag.getFloatOr("ClimbNormalZ", 0.0F))',
    )
    marker = "    public void load(CompoundTag tag) {"
    if "public void save(ValueOutput output)" not in text:
        insert = """    public void save(ValueOutput output) {
        if (this.attached) {
            output.putFloat("ClimbNormalX", (float) this.normal.x);
            output.putFloat("ClimbNormalY", (float) this.normal.y);
            output.putFloat("ClimbNormalZ", (float) this.normal.z);
        }
    }

    public void load(ValueInput input) {
        float y = input.getFloatOr("ClimbNormalY", Float.NaN);
        if (!Float.isNaN(y)) {
            Vec3 loaded = new Vec3(input.getFloatOr("ClimbNormalX", 0.0F), y, input.getFloatOr("ClimbNormalZ", 0.0F));
            this.normal = loaded.lengthSqr() > 1.0E-4D ? loaded.normalize() : UP;
            this.attached = true;
            this.grace = GRACE_TICKS;
        }
    }

"""
        idx = text.find(marker)
        if idx >= 0:
            text = text[:idx] + insert + text[idx:]
    return text


def patch_snail(text: str) -> str:
    text = text.replace("EntityDataAccessor<Vector3f> ATTACH_NORMAL", "EntityDataAccessor<Vector3fc> ATTACH_NORMAL")
    text = add_import(text, "org.joml.Vector3fc")
    text = text.replace('tag.contains("Color", 3)', 'tag.contains("Color")')
    text = text.replace('int i = tag.getInt("Color");', 'int i = tag.getIntOr("Color", 0);')
    text = re.sub(
        r"""if \(stack\.getItem\(\) instanceof DyeItem dyeItem\) \{
\s*DyeColor dyeColor = dyeItem\.getDyeColor\(\);""",
        """DyeColor dyeColor = stack.get(DataComponents.DYE);
        if (dyeColor != null) {""",
        text,
    )
    if "DataComponents.DYE" in text:
        text = add_import(text, "net.minecraft.core.component.DataComponents")
    text = re.sub(
        r"List<Player> players = this\.level\(\)\.getNearbyPlayers\(TargetingConditions\.forNonCombat\(\)\.range\(5\.0D\).*?;",
        "List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);",
        text,
        flags=re.DOTALL,
    )
    text = patch_simple_knockback(text)
    text = text.replace(
        "this.spawnAtLocation(Items.SLIME_BALL);",
        "if (this.level() instanceof ServerLevel serverLevel) { this.spawnAtLocation(serverLevel, Items.SLIME_BALL); }",
    )
    return add_import(text, "net.minecraft.server.level.ServerLevel")


def patch_close_melee(text: str) -> str:
    text = text.replace(
        "this.mob.doHurtTarget(target);",
        "if (this.mob.level() instanceof ServerLevel serverLevel) { this.mob.doHurtTarget(serverLevel, target); }",
    )
    if "ServerLevel serverLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    return text


def patch_attack_babies(text: str) -> str:
    return text.replace(
        "EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);",
        "(entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity));",
    )


def patch_breed_goal(text: str) -> str:
    text = text.replace(
        "this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)",
        "this.level.getGameRules().get(GameRules.MOB_DROPS)",
    )
    return text


def patch_thrown_duck_egg(text: str) -> str:
    text = text.replace(
        "super(NaturalistEntityTypes.DUCK_EGG.get(), livingEntity, level);",
        "super(NaturalistEntityTypes.DUCK_EGG.get(), livingEntity, level, new ItemStack(NaturalistRegistry.DUCK_EGG.get()));",
    )
    text = text.replace(
        "super(NaturalistEntityTypes.DUCK_EGG.get(), d, e, f, level);",
        "super(NaturalistEntityTypes.DUCK_EGG.get(), d, e, f, level, new ItemStack(NaturalistRegistry.DUCK_EGG.get()));",
    )
    text = text.replace(
        "new ItemParticleOption(ParticleTypes.ITEM, this.getItem())",
        "new ItemParticleOption(ParticleTypes.ITEM, this.getItem().getItem())",
    )
    text = text.replace(
        "NaturalistEntityTypes.DUCK.get().create(this.level())",
        "NaturalistEntityTypes.DUCK.get().create(this.level(), EntitySpawnReason.BREEDING)",
    )
    if "NaturalistRegistry.DUCK_EGG" in text:
        text = add_import(text, "com.crispytwig.naturalist.registry.NaturalistRegistry")
    return text


def patch_variant_nbt(text: str) -> str:
    text = re.sub(
        r'tag\.contains\(DataDrivenVariantAnimal\.VARIANT_TAG, Tag\.TAG_STRING\)',
        'tag.getString(DataDrivenVariantAnimal.VARIANT_TAG).isPresent()',
        text,
    )
    text = text.replace(
        "Identifier.tryParse(tag.getString(DataDrivenVariantAnimal.VARIANT_TAG))",
        'Identifier.tryParse(tag.getStringOr(DataDrivenVariantAnimal.VARIANT_TAG, ""))',
    )
    text = re.sub(
        r'tag\.contains\(DataDrivenVariantAnimal\.VARIANT_TAG, Tag\.TAG_ANY_NUMERIC\)',
        'tag.getInt(DataDrivenVariantAnimal.VARIANT_TAG).isPresent()',
        text,
    )
    text = text.replace(
        "Math.floorMod(tag.getInt(DataDrivenVariantAnimal.VARIANT_TAG), legacyNames.length)",
        "Math.floorMod(tag.getIntOr(DataDrivenVariantAnimal.VARIANT_TAG, 0), legacyNames.length)",
    )
    return text


def patch_legacy_variant(text: str) -> str:
    text = text.replace(
        '!tag.contains("id", Tag.TAG_STRING)',
        'tag.getString("id").isEmpty()',
    )
    text = text.replace('String id = tag.getString("id");', 'String id = tag.getStringOr("id", "");')
    return text


def patch_ant_hill_be(text: str) -> str:
    text = add_import(text, "net.minecraft.core.UUIDUtil")
    text = add_import(text, "net.minecraft.world.level.storage.ValueInput")
    text = add_import(text, "net.minecraft.world.level.storage.ValueOutput")
    old = re.compile(
        r"""    @Override
    protected void saveAdditional\(@NotNull CompoundTag tag, HolderLookup\.@NotNull Provider registries\) \{
.*?
    \}

    @Override
    protected void loadAdditional\(@NotNull CompoundTag tag, HolderLookup\.@NotNull Provider registries\) \{
.*?
    \}""",
        re.DOTALL,
    )
    new = """    @Override
    protected void saveAdditional(@NotNull ValueOutput output) {
        super.saveAdditional(output);
        if (this.owner != null) {
            output.putIntArray("Owner", UUIDUtil.uuidToIntArray(this.owner));
        }
        this.storage.storeAsItemList(output.list("Storage", ItemStack.CODEC));
    }

    @Override
    protected void loadAdditional(@NotNull ValueInput input) {
        super.loadAdditional(input);
        this.owner = input.getIntArray("Owner").filter(a -> a.length == 4).map(UUIDUtil::uuidFromIntArray).orElse(null);
        this.storage.fromItemList(input.listOrEmpty("Storage", ItemStack.CODEC));
    }"""
    return old.sub(new, text, count=1)


def patch_egg_and_simple_blocks(text: str, name: str) -> str:
    text = text.replace("GameRules.RULE_MOBGRIEFING", "GameRules.MOB_GRIEFING")
    # Calls occur on Level; guard/cast to server where game rules are available.
    text = re.sub(
        r"level\.getGameRules\(\)\.getBoolean\(GameRules\.MOB_GRIEFING\)",
        "(level instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING))",
        text,
    )
    if "ServerLevel serverLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    # Removed world time interpolation is only used as a hatch-window gate here.
    text = re.sub(r"float timeOfDay = level\.getTimeOfDay\(1\.0F\);", "float timeOfDay = level.isBrightOutside() ? 0.25F : 0.75F;", text)
    text = text.replace(
        "super.getCloneItemStack(level, pos, state);",
        "super.getCloneItemStack(level, pos, state, true);",
    )

    # Block neighbour update callback was reordered and gained scheduled-tick/random contexts.
    if "BlockState updateShape(" in text and "ScheduledTickAccess" not in text:
        text = add_import(text, "net.minecraft.world.level.ScheduledTickAccess")
        text = add_import(text, "net.minecraft.util.RandomSource")
        text = add_import(text, "net.minecraft.world.level.LevelReader")
        if name == "StarfishBlock.java":
            text = text.replace(
                "public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {",
                "protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess ticks, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource random) {",
            )
            text = text.replace(
                "return super.updateShape(state, direction, neighborState, level, pos, neighborPos);",
                "return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);",
            )
        elif name == "SnailEggBlock.java":
            text = text.replace(
                "public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {",
                "protected @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess ticks, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource random) {",
            )
            text = text.replace(
                "return super.updateShape(state, direction, neighborState, level, pos, neighborPos);",
                "return super.updateShape(state, level, ticks, pos, direction, neighborPos, neighborState, random);",
            )
        elif name == "ChrysalisBlock.java":
            text = text.replace(
                "public @NotNull BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {",
                "protected @NotNull BlockState updateShape(BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess ticks, @NotNull BlockPos currentPos, @NotNull Direction facing, @NotNull BlockPos facingPos, @NotNull BlockState facingState, @NotNull RandomSource random) {",
            )
            text = text.replace(
                "super.updateShape(state, facing, facingState, level, currentPos, facingPos)",
                "super.updateShape(state, level, ticks, currentPos, facing, facingPos, facingState, random)",
            )
    return text


def patch_hedgehog_item(text: str) -> str:
    text = text.replace(
        'return tag.contains("Health", 99) ? tag.getFloat("Health") : MAX_HEALTH;',
        'return tag.getFloatOr("Health", MAX_HEALTH);',
    )
    text = text.replace(
        "player.getCooldowns().addCooldown(this, 20);",
        "player.getCooldowns().addCooldown(stack, 20);",
    )
    return text


def patch_snail_item(text: str) -> str:
    text = text.replace('tag.contains("Color", 3)', 'tag.contains("Color")')
    text = text.replace('Snail.Color.getTypeById(tag.getInt("Color"))', 'Snail.Color.getTypeById(tag.getIntOr("Color", 0))')
    return text


def patch_queen_ant_item(text: str) -> str:
    return text.replace(
        "new DustParticleOptions(new Vector3f(1.0F, 0.2F, 0.2F), 1.0F)",
        "new DustParticleOptions(0xFFFF3333, 1.0F)",
    )


def patch_potions(text: str) -> str:
    text = text.replace(
        "() -> new Potion(new MobEffectInstance(",
        '() -> new Potion("", new MobEffectInstance(',
    )
    return text


def patch_file(path: Path, text: str) -> str:
    text = common(text)
    name = path.name

    if name in {"Rhino.java", "Alligator.java", "Hippo.java", "GiantIsopod.java", "Snail.java"}:
        # Some receive additional targeted transforms below.
        if name not in {"Rhino.java", "Hippo.java", "GiantIsopod.java", "Snail.java"}:
            text = patch_simple_knockback(text)

    if name == "Vulture.java":
        text = patch_vulture(text)
    elif name == "GreatWhiteShark.java":
        text = patch_great_white(text)
    elif name == "Rhino.java":
        text = patch_rhino(text)
    elif name == "Ostrich.java":
        text = patch_ostrich(text)
    elif name == "Snake.java":
        text = patch_snake(text)
    elif name == "Hippo.java":
        text = patch_hippo(text)
    elif name == "Giraffe.java":
        text = patch_giraffe(text)
    elif name == "Duck.java":
        text = patch_duck(text)
    elif name == "Blobfish.java":
        text = patch_blobfish(text)
    elif name == "GiantIsopod.java":
        text = patch_giant_isopod(text)
    elif name == "Snail.java":
        text = patch_snail(text)
    elif name == "SurfaceClimbing.java":
        text = patch_surface_climbing(text)
    elif name == "CloseMeleeAttackGoal.java":
        text = patch_close_melee(text)
    elif name == "AttackPlayerNearBabiesGoal.java":
        text = patch_attack_babies(text)
    elif name == "EggLayingBreedGoal.java":
        text = patch_breed_goal(text)
    elif name == "ThrownDuckEgg.java":
        text = patch_thrown_duck_egg(text)
    elif name == "MobVariantUtil.java":
        text = patch_variant_nbt(text)
    elif name == "LegacyVariantRemap.java":
        text = patch_legacy_variant(text)
    elif name == "AntHillBlockEntity.java":
        text = patch_ant_hill_be(text)
    elif name in {
        "OstrichEggBlock.java", "SnailEggBlock.java", "TortoiseEggBlock.java",
        "AntHillBlock.java", "AlligatorEggBlock.java", "ChrysalisBlock.java",
        "StarfishBlock.java",
    }:
        text = patch_egg_and_simple_blocks(text, name)
    elif name == "HedgehogItem.java":
        text = patch_hedgehog_item(text)
    elif name == "SnailItem.java":
        text = patch_snail_item(text)
    elif name == "QueenAntItem.java":
        text = patch_queen_ant_item(text)
    elif name == "NaturalistPotions.java":
        text = patch_potions(text)

    return text


def main():
    changed = []
    for root in ROOTS:
        if not root.exists():
            continue
        for path in root.rglob("*.java"):
            original = path.read_text(encoding="utf-8")
            migrated = patch_file(path, original)
            if migrated != original:
                path.write_text(migrated, encoding="utf-8")
                changed.append(str(path))
    print(f"26.2 structural wave 5 changed {len(changed)} files")
    for p in changed:
        print(p)


if __name__ == "__main__":
    main()
