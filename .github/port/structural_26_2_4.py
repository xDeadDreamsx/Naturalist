from pathlib import Path
import re

ROOT = Path("common/src/main/java/com/crispytwig/naturalist")


def add_import(text: str, qualified: str) -> str:
    line = f"import {qualified};\n"
    if line in text:
        return text
    imports = list(re.finditer(r"^import .+;\n", text, re.MULTILINE))
    if imports:
        i = imports[-1].end()
        return text[:i] + line + text[i:]
    return text


def common(text: str) -> str:
    # Entity fall distance is double in 26.2.
    text = text.replace(
        "causeFallDamage(float fallDistance, float multiplier",
        "causeFallDamage(double fallDistance, float multiplier",
    )

    # Common day/night helpers were removed in favour of environment brightness.
    text = text.replace("this.level().isDay()", "this.level().isBrightOutside()")
    text = text.replace("this.level().isNight()", "!this.level().isBrightOutside()")
    text = text.replace("level.isDay()", "level.isBrightOutside()")
    text = text.replace("level.isNight()", "!level.isBrightOutside()")

    # Entity impulse tracking field was renamed.
    text = text.replace("this.hasImpulse = true;", "this.needsSync = true;")

    # SoundEvents entries are holders in 26.2.
    for event in ("GENERIC_EAT", "PIG_STEP", "SALMON_FLOP"):
        text = text.replace(f"SoundEvents.{event},", f"SoundEvents.{event}.value(),")

    # Remaining protected random access.
    text = text.replace("level().random", "level().getRandom()")
    text = text.replace("this.level().random", "this.level().getRandom()")
    return text


def patch_sleep_window(text: str) -> str:
    # Naturalist's old 6000..13000 daytime sleep window maps best to the
    # remaining server-visible daylight predicate. Keep all other guards.
    text = re.sub(
        r"\s*long dayTime = this\.level\(\)\.getDayTime\(\)(?: % 24000)?;\n(\s*)",
        "\n\\1",
        text,
    )
    text = text.replace(
        "return dayTime > 6000 && dayTime < 13000 && ",
        "return this.level().isBrightOutside() && ",
    )
    text = text.replace(
        "return dayTime > 6000 && dayTime < 13000;",
        "return this.level().isBrightOutside();",
    )
    return text


def patch_caterpillar(text: str) -> str:
    return text.replace(
        "caterpillar.getNavigation().moveTo(logPos.getX() + 0.5D, logPos.getY() + 1.0D, this.speedModifier);",
        "caterpillar.getNavigation().moveTo(logPos.getX() + 0.5D, logPos.getY() + 1.0D, logPos.getZ() + 0.5D, this.speedModifier);",
    )


def patch_bass(text: str) -> str:
    return text.replace(
        "new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(NaturalistRegistry.BASS.get()))",
        "new ItemParticleOption(ParticleTypes.ITEM, NaturalistRegistry.BASS.get())",
    )


def patch_butterfly(text: str) -> str:
    text = text.replace('tag.getLong("HuntingCooldown")', 'tag.getLongOr("HuntingCooldown", 0L)')
    text = text.replace("stack.is(ItemTags.FLOWERS)", "stack.is(FLOWERS)")
    text = text.replace("Ingredient.of(ItemTags.FLOWERS)", "Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(FLOWERS))")
    text = re.sub(
        r"\n\s*@Override\n\s*public boolean isBaby\(\) \{\n\s*return false;\n\s*\}\n",
        "\n",
        text,
        count=1,
    )
    if "FLOWERS" in text and "private static final TagKey<Item> FLOWERS" not in text:
        marker = "public class Butterfly"
        idx = text.find("{", text.find(marker))
        if idx >= 0:
            text = text[:idx+1] + '\n    private static final TagKey<Item> FLOWERS = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("flowers"));\n' + text[idx+1:]
        for imp in (
            "net.minecraft.core.registries.BuiltInRegistries",
            "net.minecraft.core.registries.Registries",
            "net.minecraft.tags.TagKey",
            "net.minecraft.world.item.Item",
        ):
            text = add_import(text, imp)
    return text


def patch_bird(text: str) -> str:
    text = re.sub(
        r"\n\s*public boolean isBaby\(\) \{\n\s*return false;\n\s*\}\n",
        "\n",
        text,
        count=1,
    )
    text = text.replace("this.isInvulnerableTo(source)", "this.isInvulnerableTo(level, source)")
    text = text.replace(
        "this.toAvoid = this.bird.level().getNearestPlayer(this.avoidTargeting, this.bird);",
        "this.toAvoid = this.bird.level().getNearestPlayer(this.bird.getX(), this.bird.getY(), this.bird.getZ(), MAX_DIST, entity -> entity instanceof Player player && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && !player.isDiscrete());",
    )
    return text


def patch_lizard_tail(text: str) -> str:
    text = text.replace(
        "public void knockback(double strength, double x, double z) {\n        super.knockback(strength * 1.5D, x, z);\n    }",
        "public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {\n        super.knockback(strength * 1.5D, x, z, source, sourceStrength);\n    }",
    )
    if "DamageSource source" in text:
        text = add_import(text, "net.minecraft.world.damagesource.DamageSource")
    return text


def patch_starfish(text: str) -> str:
    return text.replace(
        "this.spawnAtLocation(this.getPlaceableBlock());",
        "this.spawnAtLocation(level, this.getPlaceableBlock());",
    )


def patch_mole(text: str) -> str:
    return text.replace(
        "NaturalistEntityTypes.DIRT_TRAIL.get().create(this.level())",
        "NaturalistEntityTypes.DIRT_TRAIL.get().create(this.level(), EntitySpawnReason.TRIGGERED)",
    )


def patch_scorpion(text: str) -> str:
    return text.replace("SoundEvents.GENERIC_EAT,", "SoundEvents.GENERIC_EAT.value(),")


def patch_anglerfish(text: str) -> str:
    text = text.replace(
        "this.mob.doHurtTarget(target);",
        "if (this.mob.level() instanceof ServerLevel serverLevel) { this.mob.doHurtTarget(serverLevel, target); }",
    )
    if "ServerLevel serverLevel" in text:
        text = add_import(text, "net.minecraft.server.level.ServerLevel")
    return text


def patch_boar(text: str) -> str:
    old = '''    @Override
    public void setRemainingPersistentAngerTime(int remainingPersistentAngerTime) {
        this.remainingPersistentAngerTime = remainingPersistentAngerTime;
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.remainingPersistentAngerTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID persistentAngerTarget) {
        this.persistentAngerTarget = persistentAngerTarget;
    }'''
    new = '''    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable EntityReference<LivingEntity> persistentAngerTarget) {
        this.persistentAngerTarget = persistentAngerTarget;
    }'''
    text = text.replace(old, new)
    text = text.replace("EntityTypes.ZOGLIN.create(level)", "EntityTypes.ZOGLIN.create(level, EntitySpawnReason.CONVERSION)")
    text = text.replace("zoglin.moveTo(", "zoglin.snapTo(")
    text = text.replace(
        "this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)",
        "level.getGameRules().get(GameRules.MOB_DROPS)",
    )
    text = text.replace(
        "player.spawnAtLocation(new ItemStack(NaturalistRegistry.MUSIC_DISC_DEATH_BY_HOGS.get()));",
        "player.spawnAtLocation(level, new ItemStack(NaturalistRegistry.MUSIC_DISC_DEATH_BY_HOGS.get()));",
    )
    return text


def patch_ant(text: str) -> str:
    # NeutralMob now stores an absolute anger end time and EntityReference target.
    old = '''    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int time) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, time);
    }'''
    new = '''    @Override
    public long getPersistentAngerEndTime() {
        return this.persistentAngerEndTime;
    }

    @Override
    public void setPersistentAngerEndTime(long endTime) {
        this.persistentAngerEndTime = endTime;
    }'''
    text = text.replace(old, new)
    text = text.replace("this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME);", "this.setTimeToRemainAngry(PERSISTENT_ANGER_TIME);")
    if "private long persistentAngerEndTime" not in text:
        field_marker = "private EntityReference<LivingEntity> persistentAngerTarget;"
        text = text.replace(field_marker, field_marker + "\n    private long persistentAngerEndTime = -1L;")

    text = text.replace('compound.putUUID("CarriedFood", this.carriedFoodId);', 'compound.putIntArray("CarriedFood", UUIDUtil.uuidToIntArray(this.carriedFoodId));')
    text = text.replace(
        'this.carriedFoodId = compound.hasUUID("CarriedFood") ? compound.getUUID("CarriedFood") : null;',
        'this.carriedFoodId = compound.getIntArray("CarriedFood").filter(a -> a.length == 4).map(UUIDUtil::uuidFromIntArray).orElse(null);',
    )

    text = text.replace(
        '''if (this.isTame() && this.getOwnerUUID() != null) {
            tag.putBoolean("Tame", true);
            tag.putUUID("Owner", this.getOwnerUUID());
        }''',
        '''if (this.isTame() && this.getOwnerReference() != null) {
            tag.putBoolean("Tame", true);
            tag.putIntArray("Owner", UUIDUtil.uuidToIntArray(this.getOwnerReference().getUUID()));
        }''',
    )
    text = text.replace(
        '''if (tag.getBoolean("Tame") && tag.hasUUID("Owner")) {
            this.setOwnerUUID(tag.getUUID("Owner"));
            this.setTame(true, true);
        }''',
        '''int[] owner = tag.getIntArray("Owner").orElse(null);
        if (tag.getBooleanOr("Tame", false) && owner != null && owner.length == 4) {
            this.setOwnerReference(EntityReference.of(UUIDUtil.uuidFromIntArray(owner)));
            this.setTame(true, true);
        }''',
    )
    text = text.replace("this.setPersistentAngerTarget(player.getUUID());", "this.setPersistentAngerTarget(EntityReference.of(player));")
    text = text.replace("this.ant.getOwnerUUID()", "this.ant.getOwnerReference() == null ? null : this.ant.getOwnerReference().getUUID()")
    text = add_import(text, "net.minecraft.core.UUIDUtil")
    return text


def patch_ant_hill(text: str) -> str:
    text = text.replace("ant.setPersistentAngerTarget(player.getUUID());", "ant.setPersistentAngerTarget(EntityReference.of(player));")
    text = text.replace("ant.getOwnerUUID()", "ant.getOwnerReference() == null ? null : ant.getOwnerReference().getUUID()")
    text = text.replace("ant.setOwnerUUID(owner);", "ant.setOwnerReference(EntityReference.of(owner));")
    if "EntityReference.of" in text:
        text = add_import(text, "net.minecraft.world.entity.EntityReference")
    return text


def patch_carried_food(text: str) -> str:
    text = text.replace('tag.putUUID("Ant", this.antUUID);', 'tag.putIntArray("Ant", UUIDUtil.uuidToIntArray(this.antUUID));')
    text = text.replace(
        'this.antUUID = tag.hasUUID("Ant") ? tag.getUUID("Ant") : null;',
        'this.antUUID = tag.getIntArray("Ant").filter(a -> a.length == 4).map(UUIDUtil::uuidFromIntArray).orElse(null);',
    )
    if "UUIDUtil" in text:
        text = add_import(text, "net.minecraft.core.UUIDUtil")
    return text


def patch_crab(text: str) -> str:
    # Hand-captured crab data: use ItemStack's registry-aware codec instead of removed save/parse helpers.
    text = text.replace(
        'tag.put("HeldItem", held.save(this.level().registryAccess()));',
        'ItemStack.CODEC.encodeStart(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), held).result().ifPresent(encoded -> tag.put("HeldItem", encoded));',
    )
    text = text.replace(
        'ItemStack held = ItemStack.parse(this.level().registryAccess(), tag.getCompound("HeldItem")).orElse(ItemStack.EMPTY);',
        'ItemStack held = tag.get("HeldItem").flatMap(encoded -> ItemStack.CODEC.parse(this.registryAccess().createSerializationContext(NbtOps.INSTANCE), encoded).result()).orElse(ItemStack.EMPTY);',
    )
    if "NbtOps.INSTANCE" in text:
        text = add_import(text, "net.minecraft.nbt.NbtOps")

    text = text.replace(
        'List<Player> players = this.level().getNearbyPlayers(TargetingConditions.forNonCombat().range(4.0D).selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)), this, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));',
        'List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);',
    )
    text = text.replace(
        "public void knockback(double strength, double x, double z) {\n        super.knockback(this.canHide() ? strength / 4 : strength, x, z);\n    }",
        "public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {\n        super.knockback(this.canHide() ? strength / 4 : strength, x, z, source, sourceStrength);\n    }",
    )
    return text


def patch_tortoise(text: str) -> str:
    text = text.replace(
        "tortoise.setOwnerUUID(this.random.nextBoolean() ? tortoiseParent.getOwnerUUID() : this.getOwnerUUID());",
        "tortoise.setOwnerReference(this.random.nextBoolean() ? tortoiseParent.getOwnerReference() : this.getOwnerReference());",
    )
    text = text.replace(
        'List<Player> players = this.level().getNearbyPlayers(TargetingConditions.forNonCombat().range(5.0D).selector((livingEntity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity) && !livingEntity.isDiscrete() && !livingEntity.isHolding(TEMPT_ITEMS)), this, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D));',
        'List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D), player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && !player.isDiscrete() && !player.isHolding(TEMPT_ITEMS));',
    )
    text = text.replace(
        '''public void knockback(double strength, double x, double z) {
        if (this.isBaby()) {
            super.knockback(strength / Math.max(1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.01), x, z);
        } else {
            super.knockback(this.isInSittingPose() || this.canHide() ? strength / 4 : strength, x, z);
        }
    }''',
        '''public void knockback(double strength, double x, double z, DamageSource source, float sourceStrength) {
        if (this.isBaby()) {
            super.knockback(strength / Math.max(1.0 - this.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.01), x, z, source, sourceStrength);
        } else {
            super.knockback(this.isInSittingPose() || this.canHide() ? strength / 4 : strength, x, z, source, sourceStrength);
        }
    }''',
    )
    return text


def patch_hedgehog(text: str) -> str:
    text = re.sub(
        r'''if \(!this\.throwEnchantments\.keySet\(\)\.isEmpty\(\)\) \{\n\s*ItemEnchantments\.CODEC\.encodeStart\(this\.registryAccess\(\)\.createSerializationContext\(NbtOps\.INSTANCE\), this\.throwEnchantments\)\n\s*\.result\(\)\.ifPresent\(tag -> compound\.put\("ThrowEnchantments", tag\)\);\n\s*\}''',
        'if (!this.throwEnchantments.keySet().isEmpty()) {\n            compound.store("ThrowEnchantments", ItemEnchantments.CODEC, this.throwEnchantments);\n        }',
        text,
    )
    text = re.sub(
        r'''if \(compound\.contains\("ThrowEnchantments"\)\) \{\n\s*ItemEnchantments\.CODEC\.parse\(this\.registryAccess\(\)\.createSerializationContext\(NbtOps\.INSTANCE\), compound\.get\("ThrowEnchantments"\)\)\n\s*\.result\(\)\.ifPresent\(this::setThrowEnchantments\);\n\s*\}''',
        'compound.read("ThrowEnchantments", ItemEnchantments.CODEC).ifPresent(this::setThrowEnchantments);',
        text,
    )
    text = text.replace(
        'List<Player> players = this.level().getNearbyPlayers(TargetingConditions.forNonCombat().range(6.0).selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity)), this, this.getBoundingBox().inflate(6.0, 3.0, 6.0));',
        'List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(6.0, 3.0, 6.0), EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);',
    )
    text = text.replace(
        "public boolean isInvulnerableTo(@NotNull DamageSource source) {\n        return super.isInvulnerableTo(source)",
        "public boolean isInvulnerableTo(@NotNull ServerLevel level, @NotNull DamageSource source) {\n        return super.isInvulnerableTo(level, source)",
    )
    # hurt() is side-effect-only now; the server variant preserves its old success check.
    text = text.replace(
        'if (target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F + this.getThrowEnchantmentLevel(Enchantments.THORNS))) {',
        'DamageSource throwSource = this.damageSources().thrown(this, this.getOwner());\n                        if (target.hurtServer((ServerLevel) this.level(), throwSource, 2.0F + this.getThrowEnchantmentLevel(Enchantments.THORNS))) {',
    )
    text = text.replace(
        'target.knockback(punch * 0.6, -motion.x, -motion.z);',
        'target.knockback(punch * 0.6, -motion.x, -motion.z, throwSource, 0.0F);',
    )
    text = text.replace("registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)", "registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)")
    text = text.replace("registryAccess().registryOrThrow(Registries.ENCHANTMENT)", "registryAccess().lookupOrThrow(Registries.ENCHANTMENT)")
    text = text.replace(".getHolderOrThrow(NaturalistDamageTypes.HEDGEHOG_THROW)", ".getOrThrow(NaturalistDamageTypes.HEDGEHOG_THROW)")
    text = text.replace(".getHolder(key)", ".get(key)")
    return text


def patch_vulture(text: str) -> str:
    # Preserve attack result using the server damage API.
    text = text.replace(
        'if (shouldHurt == target.hurt(target.damageSources().mobAttack(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {',
        'DamageSource attackSource = target.damageSources().mobAttack(this);\n        if (shouldHurt == target.hurtServer(level, attackSource, (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE))) {',
    )
    text = text.replace(
        '((LivingEntity)target).knockback(knockback * 0.5f, Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(this.getYRot() * Mth.DEG_TO_RAD));',
        '((LivingEntity)target).knockback(knockback * 0.5f, Mth.sin(this.getYRot() * Mth.DEG_TO_RAD), -Mth.cos(this.getYRot() * Mth.DEG_TO_RAD), attackSource, 0.0F);',
    )
    text = text.replace(
        "public boolean isInvulnerableTo(@NotNull DamageSource source) {\n        return source.equals(this.damageSources().cactus()) || super.isInvulnerableTo(source);\n    }",
        "public boolean isInvulnerableTo(@NotNull ServerLevel level, @NotNull DamageSource source) {\n        return source.equals(this.damageSources().cactus()) || super.isInvulnerableTo(level, source);\n    }",
    )
    text = text.replace("this.spawnAtLocation(itemStack);", "this.spawnAtLocation((ServerLevel) this.level(), itemStack);")
    # Mob already performs the generic loot scan in 26.2. Remove the duplicated old scan/profiler block.
    text = re.sub(
        r'\n\s*this\.level\(\)\.getProfiler\(\)\.push\("looting"\);.*?this\.level\(\)\.getProfiler\(\)\.pop\(\);',
        '',
        text,
        flags=re.DOTALL,
    )
    text = text.replace("this.playEatingSound();", "this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);")
    text = text.replace(
        "this.mob.doHurtTarget(enemy);",
        "if (this.mob.level() instanceof ServerLevel serverLevel) { this.mob.doHurtTarget(serverLevel, enemy); }",
    )
    text = text.replace(
        "this.toAvoid = this.vulture.level().getNearestPlayer(this.fleeConditions, this.vulture);",
        "this.toAvoid = this.vulture.level().getNearestPlayer(this.vulture.getX(), this.vulture.getY(), this.vulture.getZ(), 8.0D, entity -> entity instanceof Player player && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player));",
    )
    return text


def patch_file(path: Path, text: str) -> str:
    text = common(text)
    name = path.name
    if name in ("Tiger.java", "Lion.java", "Capybara.java"):
        text = patch_sleep_window(text)
    if name == "Caterpillar.java": text = patch_caterpillar(text)
    elif name == "Bass.java": text = patch_bass(text)
    elif name == "Butterfly.java": text = patch_butterfly(text)
    elif name == "Bird.java": text = patch_bird(text)
    elif name == "LizardTail.java": text = patch_lizard_tail(text)
    elif name == "Starfish.java": text = patch_starfish(text)
    elif name == "Mole.java": text = patch_mole(text)
    elif name == "Scorpion.java": text = patch_scorpion(text)
    elif name == "Anglerfish.java": text = patch_anglerfish(text)
    elif name == "Boar.java": text = patch_boar(text)
    elif name == "Ant.java": text = patch_ant(text)
    elif name == "AntHillBlock.java": text = patch_ant_hill(text)
    elif name == "CarriedFoodEntity.java": text = patch_carried_food(text)
    elif name == "Crab.java": text = patch_crab(text)
    elif name == "Tortoise.java": text = patch_tortoise(text)
    elif name == "Hedgehog.java": text = patch_hedgehog(text)
    elif name == "Vulture.java": text = patch_vulture(text)
    return text


def main():
    changed = []
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        migrated = patch_file(path, original)
        if migrated != original:
            path.write_text(migrated, encoding="utf-8")
            changed.append(str(path))
    print(f"26.2 structural wave 4 changed {len(changed)} files")
    for p in changed:
        print(p)


if __name__ == "__main__":
    main()
