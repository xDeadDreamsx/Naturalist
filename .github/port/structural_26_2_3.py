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
    replacements = {
        ".isInWaterOrBubble()": ".isInWater()",
        "PathType.DANGER_FIRE": "PathType.FIRE_IN_NEIGHBOR",
        "PathType.DAMAGE_FIRE": "PathType.FIRE",
        "PathType.DANGER_OTHER": "PathType.DAMAGING_IN_NEIGHBOR",
        "PathType.DAMAGE_OTHER": "PathType.DAMAGING",
        "PathType.DANGER_POWDER_SNOW": "PathType.POWDER_SNOW",
        ".getMinBuildHeight()": ".getMinY()",
        ".getMaxBuildHeight()": ".getMaxY()",
        ".getType().is(": ".getType().builtInRegistryHolder().is(",
        ".level().random": ".level().getRandom()",
        "level.random": "level.getRandom()",
    }
    for old, new in replacements.items():
        text = text.replace(old, new)

    # FlyingPathNavigation no longer exposes this toggle.
    text = re.sub(r"^\s*\w+\.setCanPassDoors\(true\);\n", "", text, flags=re.MULTILINE)

    # TargetingConditions.Selector now takes (target, ServerLevel).
    text = text.replace(
        ".selector(EntitySelector.NO_CREATIVE_OR_SPECTATOR::test)",
        ".selector((entity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(entity))",
    )
    text = re.sub(
        r"\.selector\((\w+)\s*->",
        r".selector((\1, level) ->",
        text,
    )

    # The target-goal predicate uses the same two-argument selector in 26.2.
    # Limit the rewrite to the final predicate of each constructor expression.
    def goal_rewrite(match: re.Match) -> str:
        s = match.group(0)
        s = re.sub(r",\s*\((\w+)\)\s*->", r", (\1, level) ->", s, count=1)
        s = re.sub(r",\s*(\w+)\s*->", r", (\1, level) ->", s, count=1)
        s = s.replace("LivingEntity::isInWater", "(entity, level) -> entity.isInWater()")
        return s
    text = re.sub(r"new NearestAttackableTargetGoal<[^;]+", goal_rewrite, text)

    # Common 26.2 killedEntity signature.
    def killed_sig(m: re.Match) -> str:
        args = m.group(1)
        if "DamageSource" in args:
            return m.group(0)
        return "public boolean killedEntity(" + args + ", @NotNull DamageSource source)"
    before = text
    text = re.sub(r"public boolean killedEntity\(([^)]*ServerLevel\s+level[^)]*LivingEntity\s+killed[^)]*)\)", killed_sig, text)
    text = text.replace("super.killedEntity(level, killed)", "super.killedEntity(level, killed, source)")
    if text != before and "DamageSource source" in text:
        text = add_import(text, "net.minecraft.world.damagesource.DamageSource")

    # Most Naturalist uses only need the item identity for item particles now.
    text = re.sub(
        r"new ItemParticleOption\(ParticleTypes\.ITEM, new ItemStack\(([^()]+)\)\)",
        r"new ItemParticleOption(ParticleTypes.ITEM, \1)",
        text,
    )
    for name in ("eatingStack", "itemStack", "particleStack"):
        text = text.replace(
            f"new ItemParticleOption(ParticleTypes.ITEM, {name})",
            f"new ItemParticleOption(ParticleTypes.ITEM, {name}.getItem())",
        )
    text = text.replace(
        "new ItemParticleOption(ParticleTypes.ITEM, stack.copy())",
        "new ItemParticleOption(ParticleTypes.ITEM, stack.getItem())",
    )

    # Old helper was removed; TamableAnimal retains playEatingSound().
    text = re.sub(
        r"this\.playSound\(this\.getEatingSound\([^)]*\),\s*1\.0[fF]?,\s*1\.0[fF]?\);",
        "this.playEatingSound();",
        text,
    )

    return text


def patch_mob_part(text: str) -> str:
    return text.replace(
        "!this.isInvulnerableTo(level, source) && this.parent.hurtServer(level, source, amount)",
        "!this.isInvulnerableToBase(source) && this.parent.hurtServer(level, source, amount)",
    )


def patch_bear(text: str) -> str:
    text = text.replace("public boolean canTakeItem(@NotNull ItemStack itemStack)", "public boolean canHoldItem(@NotNull ItemStack itemStack)")
    text = text.replace("super.canTakeItem(itemStack)", "super.canHoldItem(itemStack)")
    text = text.replace("protected void pickUpItem(@NotNull ItemEntity itemEntity)", "protected void pickUpItem(@NotNull ServerLevel level, @NotNull ItemEntity itemEntity)")
    text = text.replace("this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;", "this.setDropChance(EquipmentSlot.MAINHAND, 2.0F);")

    # Vanilla Mob handles the item scan in 26.2; keep Bear's canHoldItem/pickUpItem customization only.
    text = re.sub(
        r"\n\s*this\.level\(\)\.getProfiler\(\)\.push\(\"looting\"\);.*?this\.level\(\)\.getProfiler\(\)\.pop\(\);",
        "",
        text,
        flags=re.DOTALL,
    )

    text = text.replace(
        "Predicate<LivingEntity> targetPredicate",
        "TargetingConditions.Selector targetPredicate",
    )
    if "TargetingConditions.Selector" in text:
        text = add_import(text, "net.minecraft.world.entity.ai.targeting.TargetingConditions")

    text = text.replace(
        "if (bear.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {",
        "if (bear.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {",
    )
    text = text.replace(
        "BeehiveBlock.dropHoneycomb(bear.level(), blockPos);",
        "if (bear.level() instanceof ServerLevel serverLevel) {\n                BeehiveBlock.dropHoneycomb(serverLevel, new ItemStack(Items.SHEARS), state, serverLevel.getBlockEntity(blockPos), bear, blockPos);\n            }",
    )
    text = text.replace(
        "bear.spawnAtLocation(stack);",
        "if (bear.level() instanceof ServerLevel serverLevel) {\n                    bear.spawnAtLocation(serverLevel, stack);\n                }",
    )
    return text


def patch_baby_sniff(text: str) -> str:
    return text.replace(
        "this.mob.isWithinRestriction(mutable) && this.isValidTarget(this.mob.level(), mutable)",
        "this.isValidTarget(this.mob.level(), mutable)",
    )


def patch_tiger(text: str) -> str:
    text = re.sub(
        r"long dayTime = this\.level\(\)\.getDayTime\(\);\s*return ([^;]*dayTime[^;]*);",
        "return this.level().isBrightOutside() && !this.isInWater() && !this.isOnFire() && this.onGround();",
        text,
    )
    return text


def patch_animation_sound(text: str) -> str:
    return text.replace(
        "this.state.getAccumulatedTime() / 1000.0F",
        "this.state.getTimeInMillis(entity.tickCount) / 1000.0F",
    )


def patch_firefly(text: str) -> str:
    text = text.replace("public boolean causeFallDamage(float fallDistance, float multiplier", "public boolean causeFallDamage(double fallDistance, float multiplier")
    text = text.replace("this.isSunBurnTick()", "this.naturalist$isSunBurnTick()")
    text = text.replace("firefly.isSunBurnTick()", "firefly.naturalist$isSunBurnTick()")
    text = text.replace("protected boolean isSunBurnTick()", "private boolean naturalist$isSunBurnTick()")
    text = text.replace("    @Override\n    private boolean naturalist$isSunBurnTick()", "    private boolean naturalist$isSunBurnTick()")
    text = text.replace("this.level().isNight()", "!this.level().isBrightOutside()")
    text = text.replace("this.level().isDay()", "this.level().isBrightOutside()")
    return text


def patch_catchable(text: str) -> str:
    for key in ("NoAI", "Silent", "NoGravity", "Glowing", "Invulnerable"):
        text = text.replace(f'tag.getBoolean("{key}")', f'tag.getBooleanOr("{key}", false)')
    text = text.replace('tag.contains("Health", 99)', 'tag.contains("Health")')
    text = text.replace('tag.getFloat("Health")', 'tag.getFloatOr("Health", mob.getHealth())')

    old_save = '''        if (entity.isTame() && entity.getOwnerUUID() != null) {
            tag.putBoolean("Tame", true);
            tag.putUUID("Owner", entity.getOwnerUUID());
            tag.putBoolean("FollowingOwner", entity.isFollowingOwner());
            tag.putBoolean("Sitting", entity.isOrderedToSit());
        }'''
    new_save = '''        EntityReference<LivingEntity> owner = entity.getOwnerReference();
        if (entity.isTame() && owner != null) {
            tag.putBoolean("Tame", true);
            tag.putIntArray("Owner", UUIDUtil.uuidToIntArray(owner.getUUID()));
            tag.putBoolean("FollowingOwner", entity.isFollowingOwner());
            tag.putBoolean("Sitting", entity.isOrderedToSit());
        }'''
    text = text.replace(old_save, new_save)

    old_load = '''        if (tag.getBoolean("Tame") && tag.hasUUID("Owner")) {
            entity.setOwnerUUID(tag.getUUID("Owner"));
            entity.setTame(true, true);
            entity.setFollowingOwner(tag.getBoolean("FollowingOwner"));
            entity.setOrderedToSit(tag.getBoolean("Sitting"));
        }'''
    new_load = '''        int[] owner = tag.getIntArray("Owner").orElse(null);
        if (tag.getBooleanOr("Tame", false) && owner != null && owner.length == 4) {
            entity.setOwnerReference(EntityReference.of(UUIDUtil.uuidFromIntArray(owner)));
            entity.setTame(true, true);
            entity.setFollowingOwner(tag.getBooleanOr("FollowingOwner", false));
            entity.setOrderedToSit(tag.getBooleanOr("Sitting", false));
        }'''
    text = text.replace(old_load, new_load)
    if "EntityReference<LivingEntity>" in text:
        text = add_import(text, "net.minecraft.world.entity.EntityReference")
        text = add_import(text, "net.minecraft.core.UUIDUtil")
    return text


def patch_rat(text: str) -> str:
    text = re.sub(
        r"\s*if \(this\.workstationPos != null\) \{\s*this\.restrictTo\(this\.workstationPos, WORK_RADIUS\);\s*\} else \{\s*this\.clearRestriction\(\);\s*\}",
        "",
        text,
    )
    text = text.replace('this.setAge(tag.getInt("Age"));', 'this.setAge(tag.getIntOr("Age", 0));')
    text = text.replace('baby.setOwnerUUID(this.getOwnerUUID());', 'baby.setOwnerReference(this.getOwnerReference());')
    text = text.replace('this.level().isDay()', 'this.level().isBrightOutside()')

    # ValueInput/Output container persistence.
    text = text.replace(
        'compound.put("CarriedItems", this.carriedItems.createTag(this.registryAccess()));',
        '''NonNullList<ItemStack> carried = NonNullList.withSize(this.carriedItems.getContainerSize(), ItemStack.EMPTY);
        for (int i = 0; i < carried.size(); i++) carried.set(i, this.carriedItems.getItem(i));
        ContainerHelper.saveAllItems(compound.child("CarriedItems"), carried);''',
    )
    text = re.sub(
        r'''if \(compound\.contains\("CarriedItems", 9\)\) \{\s*this\.carriedItems\.fromTag\(compound\.getList\("CarriedItems", 10\), this\.registryAccess\(\)\);\s*\}''',
        '''NonNullList<ItemStack> carried = NonNullList.withSize(this.carriedItems.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound.childOrEmpty("CarriedItems"), carried);
        for (int i = 0; i < carried.size(); i++) this.carriedItems.setItem(i, carried.get(i));''',
        text,
    )
    if "NonNullList<ItemStack> carried" in text:
        text = add_import(text, "net.minecraft.core.NonNullList")
        text = add_import(text, "net.minecraft.world.ContainerHelper")

    old_nearest = 'this.player = this.rat.level().getNearestPlayer(this.begTargeting, this.rat);'
    new_nearest = '''if (this.rat.level() instanceof ServerLevel serverLevel) {
                this.player = serverLevel.getNearestPlayer(this.rat.getX(), this.rat.getY(), this.rat.getZ(), 4.0D,
                        entity -> entity instanceof Player p && this.begTargeting.test(serverLevel, this.rat, p));
            } else {
                this.player = null;
            }'''
    text = text.replace(old_nearest, new_nearest)
    return text


def patch_simple_age(text: str) -> str:
    text = re.sub(r'this\.setAge\(tag\.getInt\("Age"\)\);', 'this.setAge(tag.getIntOr("Age", 0));', text)
    return text


def patch_owner_offspring(text: str) -> str:
    text = text.replace('baby.setOwnerUUID(this.getOwnerUUID());', 'baby.setOwnerReference(this.getOwnerReference());')
    return text


def patch_piranha(text: str) -> str:
    return text


def patch_file(path: Path, text: str) -> str:
    text = common(text)
    name = path.name
    if name == "MobPart.java": text = patch_mob_part(text)
    elif name == "Bear.java": text = patch_bear(text)
    elif name == "BabySniffFlowersGoal.java": text = patch_baby_sniff(text)
    elif name == "Tiger.java": text = patch_tiger(text)
    elif name == "AnimationSoundPlayer.java": text = patch_animation_sound(text)
    elif name == "Firefly.java": text = patch_firefly(text)
    elif name == "Catchable.java": text = patch_catchable(text)
    elif name == "Rat.java": text = patch_rat(text)
    text = patch_simple_age(text)
    text = patch_owner_offspring(text)
    return text


def main():
    changed = []
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        migrated = patch_file(path, original)
        if migrated != original:
            path.write_text(migrated, encoding="utf-8")
            changed.append(str(path))
    print(f"26.2 structural wave 3 changed {len(changed)} files")
    for p in changed: print(p)


if __name__ == "__main__":
    main()
