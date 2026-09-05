#!/usr/bin/env python3
"""Restore additional 1.21.1 animal behavior semantics on Minecraft 26.2.

The earlier API migration kept the goal lists but several helper behaviors were broadened or
removed while adapting changed Level/Mob APIs. This pass restores Naturalist's own semantics:
nearby-player distance checks and filters, rattlesnake creative-player behavior, the larger
manual food pickup scans used by bears/vultures, and the elephant's custom attack knockback.
"""

from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob")


def replace_method(path: Path, marker: str, replacement: str) -> bool:
    text = path.read_text(encoding="utf-8")
    if replacement in text:
        return False
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f"Could not locate {marker!r} in {path}")
    brace = text.find("{", start)
    if brace < 0:
        raise RuntimeError(f"Could not locate method body for {marker!r} in {path}")

    depth = 0
    i = brace
    in_string = False
    in_char = False
    escaped = False
    line_comment = False
    block_comment = False
    while i < len(text):
        c = text[i]
        n = text[i + 1] if i + 1 < len(text) else ""
        if line_comment:
            if c == "\n":
                line_comment = False
        elif block_comment:
            if c == "*" and n == "/":
                block_comment = False
                i += 1
        elif in_string:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif c == '"':
                in_string = False
        elif in_char:
            if escaped:
                escaped = False
            elif c == "\\":
                escaped = True
            elif c == "'":
                in_char = False
        else:
            if c == "/" and n == "/":
                line_comment = True
                i += 1
            elif c == "/" and n == "*":
                block_comment = True
                i += 1
            elif c == '"':
                in_string = True
            elif c == "'":
                in_char = True
            elif c == "{":
                depth += 1
            elif c == "}":
                depth -= 1
                if depth == 0:
                    path.write_text(text[:start] + replacement + text[i + 1:], encoding="utf-8")
                    return True
        i += 1
    raise RuntimeError(f"Unterminated method body for {marker!r} in {path}")


def main() -> None:
    changed = []

    bear = ROOT / "Bear.java"
    bear_ai = """    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
            if (this.wakeTicks > 0) {
                this.wakeTicks--;
            }
        }
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
        this.handleEating();
        if (!this.getMainHandItem().isEmpty()) {
            if (this.isAngry()) {
                this.stopBeingAngry();
            }
            this.setSniffing(false);
        }
        if (this.level() instanceof ServerLevel serverLevel && this.canPickUpLoot() && this.isAlive()
                && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
            for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 0.0D, 1.0D))) {
                if (!itemEntity.isRemoved() && !itemEntity.getItem().isEmpty()
                        && this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
                    this.pickUpItem(serverLevel, itemEntity);
                }
            }
        }
    }
"""
    if replace_method(bear, "    public void aiStep() {", bear_ai):
        changed.append(str(bear))

    vulture = ROOT / "Vulture.java"
    vulture_ai = """    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel serverLevel && this.canPickUpLoot() && this.isAlive()
                && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING)) {
            for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 1.0D, 1.0D))) {
                if (!itemEntity.isRemoved() && !itemEntity.getItem().isEmpty()
                        && this.wantsToPickUp(serverLevel, itemEntity.getItem())) {
                    this.pickUpItem(serverLevel, itemEntity);
                }
            }
        }
        if (!this.level().isClientSide() && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            if (this.perchCooldown > 0) {
                --this.perchCooldown;
            }
            ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (stack.get(DataComponents.FOOD) != null) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack finishedStack = stack.finishUsingItem(this.level(), this);
                    if (!finishedStack.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, finishedStack);
                    }
                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1f) {
                    this.playSound(SoundEvents.GENERIC_EAT.value(), 1.0F, 1.0F);
                    this.level().broadcastEntityEvent(this, (byte)45);
                }
            }
        }
    }
"""
    if replace_method(vulture, "    public void aiStep() {", vulture_ai):
        changed.append(str(vulture))

    elephant = ROOT / "Elephant.java"
    elephant_attack = """    public boolean doHurtTarget(ServerLevel level, Entity target) {
        DamageSource attackSource = target.damageSources().mobAttack(this);
        boolean shouldHurt = target.hurtServer(level, attackSource, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (shouldHurt && target instanceof LivingEntity livingEntity) {
            Vec3 knockbackDirection = new Vec3(this.blockPosition().getX() - target.getX(), 0.0D,
                    this.blockPosition().getZ() - target.getZ()).normalize();
            float shieldBlockModifier = livingEntity.getItemBlockingWith() != null ? 0.5F : 1.0F;
            livingEntity.knockback(shieldBlockModifier * 3.0D, knockbackDirection.x(), knockbackDirection.z(), attackSource, 0.0F);
            double knockbackResistance = Math.max(0.0D, 1.0D - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(0.0D, 0.5D * knockbackResistance, 0.0D));
        }
        this.playSound(SoundEvents.RAVAGER_ATTACK, 1.0F, 1.0F);
        return shouldHurt;
    }
"""
    if replace_method(elephant, "    public boolean doHurtTarget(ServerLevel level, Entity target) {", elephant_attack):
        changed.append(str(elephant))

    proximity_methods = [
        ("Snail.java", "    public boolean canHide() {", """    public boolean canHide() {
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),
                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && this.distanceToSqr(player) <= 25.0D);
        return !players.isEmpty();
    }
"""),
        ("Tortoise.java", "    public boolean canHide() {", """    public boolean canHide() {
        if (this.isTame()) {
            return false;
        }
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),
                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)
                        && !player.isDiscrete() && !player.isHolding(temptItems())
                        && this.distanceToSqr(player) <= 25.0D);
        return !players.isEmpty();
    }
"""),
        ("Crab.java", "    private boolean thinkCanHide() {", """    private boolean thinkCanHide() {
        if (this.isBaby() || this.isTame() || !this.getMainHandItem().isEmpty()) {
            return false;
        }
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),
                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && this.distanceToSqr(player) <= 16.0D);
        boolean playerNear = false;
        for (Player player : players) {
            if (!player.isCrouching() && !foodItems().test(player.getMainHandItem()) && !foodItems().test(player.getOffhandItem())) {
                playerNear = true;
                break;
            }
        }
        return playerNear && this.findNearbyWeapon() == null;
    }
"""),
        ("Hedgehog.java", "    private boolean thinkCanHide() {", """    private boolean thinkCanHide() {
        if (this.isTame() || this.isRolling() || this.isSprinting() || this.isInSittingPose()) {
            return false;
        }
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(6.0D, 3.0D, 6.0D),
                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && this.distanceToSqr(player) <= 36.0D);
        for (Player player : players) {
            if (!player.isCrouching() && !foodItems().test(player.getMainHandItem()) && !foodItems().test(player.getOffhandItem())) {
                return true;
            }
        }
        return false;
    }
"""),
        ("Ostrich.java", "    private boolean thinkCanHide() {", """    private boolean thinkCanHide() {
        if (this.isTame() || this.isBaby() || this.isAggressive() || this.isVehicle()) {
            return false;
        }
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(16.0D, 8.0D, 16.0D),
                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player)
                        && !player.isDiscrete() && !player.isHolding(foodItems())
                        && this.distanceToSqr(player) <= 256.0D);
        return !players.isEmpty();
    }
"""),
        ("GiantIsopod.java", "    private boolean computeCanHide() {", """    private boolean computeCanHide() {
        if (this.isBaby()) {
            return false;
        }
        if (this.hideHoldTicks > 0) {
            return true;
        }
        return !this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(3.0D, 2.0D, 3.0D),
                player -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player) && this.distanceToSqr(player) <= 9.0D).isEmpty();
    }
"""),
    ]

    for filename, marker, replacement in proximity_methods:
        path = ROOT / filename
        if replace_method(path, marker, replacement):
            changed.append(str(path))

    snake = ROOT / "Snake.java"
    snake_rattle = """    private boolean canRattle() {
        boolean rattlesnake = this.isRattlesnake();
        List<Player> players = this.level().getEntitiesOfClass(Player.class,
                this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D),
                player -> !player.isSpectator() && this.distanceToSqr(player) <= 16.0D);
        if (!players.isEmpty() && rattlesnake && !players.getFirst().isCreative()) {
            this.setTarget(players.getFirst());
        } else {
            this.setTarget(null);
        }
        return !players.isEmpty() && rattlesnake;
    }
"""
    if replace_method(snake, "    private boolean canRattle() {", snake_rattle):
        changed.append(str(snake))

    print(f"26.2 additional behavior parity pass changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
