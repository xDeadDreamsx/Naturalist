from pathlib import Path

ROOT = Path("common/src/main/java/com/crispytwig/naturalist")


def patch_ostrich(text: str) -> str:
    text = text.replace(
        "    private boolean canBeSaddled() {\n        return this.isAlive() && !this.isBaby() && this.isTame();\n    }\n    }\n\n    @Override\n    protected void dropEquipment() {\n        super.dropEquipment();",
        "    private boolean canBeSaddled() {\n        return this.isAlive() && !this.isBaby() && this.isTame();\n    }\n\n    @Override\n    protected void dropEquipment(ServerLevel level) {\n        super.dropEquipment(level);",
    )
    text = text.replace(
        "this.spawnAtLocation(Items.SADDLE);",
        "if (this.level() instanceof ServerLevel serverLevel) {\n                    this.spawnAtLocation(serverLevel, Items.SADDLE);\n                }",
    )
    text = text.replace(
        ".selector(livingEntity -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)",
        ".selector((livingEntity, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingEntity)",
    )
    text = text.replace(
        "super(ostrich, Player.class, 10, true, false,\n                    entity -> !ostrich.isBaby() && !ostrich.isTame() && ostrich.isNearOwnedEgg(entity));",
        "super(ostrich, Player.class, 10, true, false,\n                    (entity, level) -> !ostrich.isBaby() && !ostrich.isTame() && ostrich.isNearOwnedEgg(entity));",
    )

    old_travel = """    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (!this.isAlive()) {
            return;
        }
        LivingEntity livingEntity = this.getControllingPassenger();
        if (!this.isVehicle() || livingEntity == null) {
            super.travel(travelVector);
            return;
        }
        this.setYRot(livingEntity.getYRot());
        this.yRotO = this.getYRot();
        this.setXRot(livingEntity.getXRot() * 0.5f);
        this.setRot(this.getYRot(), this.getXRot());
        this.yHeadRot = this.getYRot();
        this.yBodyRot = Mth.rotLerp(0.35F, this.yBodyRot, this.getYRot());
        float g = livingEntity.zza;
        if (this.playerJumpPendingScale > 0.0F && !this.isJumping && this.onGround()) {
            double jumpVelocity = this.getAttributeValue(Attributes.JUMP_STRENGTH) * this.playerJumpPendingScale * this.getBlockJumpFactor() + this.getJumpBoostPower();
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.x, jumpVelocity, deltaMovement.z);
            this.isJumping = true;
            this.hasImpulse = true;
            if (g > 0.0F) {
                float sin = Mth.sin(this.getYRot() * Mth.DEG_TO_RAD);
                float cos = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD);
                this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * sin * this.playerJumpPendingScale, 0.0D, 0.4F * cos * this.playerJumpPendingScale));
            }
            this.playerJumpPendingScale = 0.0F;
        }
        if (this.onGround() && this.playerJumpPendingScale == 0.0F) {
            this.isJumping = false;
        }
        if (this.isControlledByLocalInstance()) {
            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.4F);
            super.travel(new Vec3(livingEntity.xxa * 0.5f, travelVector.y, g));
        } else if (livingEntity instanceof Player) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        this.calculateEntityAnimation(false);
        this.tryCheckInsideBlocks();
    }
"""
    new_ride = """    @Override
    protected void tickRidden(@NotNull Player controller, @NotNull Vec3 riddenInput) {
        super.tickRidden(controller, riddenInput);
        this.setRot(controller.getYRot(), controller.getXRot() * 0.5F);
        this.yRotO = this.yBodyRot = this.yHeadRot = this.getYRot();
        float forward = controller.zza;
        if (this.playerJumpPendingScale > 0.0F && !this.isJumping && this.onGround()) {
            double jumpVelocity = this.getAttributeValue(Attributes.JUMP_STRENGTH) * this.playerJumpPendingScale * this.getBlockJumpFactor() + this.getJumpBoostPower();
            Vec3 deltaMovement = this.getDeltaMovement();
            this.setDeltaMovement(deltaMovement.x, jumpVelocity, deltaMovement.z);
            this.isJumping = true;
            this.hasImpulse = true;
            if (forward > 0.0F) {
                float sin = Mth.sin(this.getYRot() * Mth.DEG_TO_RAD);
                float cos = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD);
                this.setDeltaMovement(this.getDeltaMovement().add(-0.4F * sin * this.playerJumpPendingScale, 0.0D, 0.4F * cos * this.playerJumpPendingScale));
            }
            this.playerJumpPendingScale = 0.0F;
        }
        if (this.onGround() && this.playerJumpPendingScale == 0.0F) {
            this.isJumping = false;
        }
    }

    @Override
    protected @NotNull Vec3 getRiddenInput(@NotNull Player controller, @NotNull Vec3 selfInput) {
        return new Vec3(controller.xxa * 0.5F, 0.0D, controller.zza);
    }

    @Override
    protected float getRiddenSpeed(@NotNull Player controller) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.4F;
    }
"""
    text = text.replace(old_travel, new_ride)

    old_anger = """    private void updateEggAnger() {
        if (this.remainingPersistentAngerTime <= 0) {
            return;
        }
        LivingEntity target = this.getTarget();
        boolean engaged = target != null && target.isAlive()
                && this.persistentAngerTarget != null && this.persistentAngerTarget.matches(target)
                && this.closerThan(target, this.getAttributeValue(Attributes.FOLLOW_RANGE));
        if (!engaged && --this.remainingPersistentAngerTime <= 0) {
            this.stopBeingAngry();
        }
    }
"""
    new_anger = """    private void updateEggAnger() {
        if (this.level() instanceof ServerLevel serverLevel) {
            this.updatePersistentAnger(serverLevel, true);
        }
    }
"""
    text = text.replace(old_anger, new_anger)
    return text


def patch_elephant(text: str) -> str:
    return text


def main() -> None:
    changed = []
    for path in ROOT.rglob("*.java"):
        original = path.read_text(encoding="utf-8")
        migrated = original
        if path.name == "Ostrich.java":
            migrated = patch_ostrich(migrated)
        elif path.name == "Elephant.java":
            migrated = patch_elephant(migrated)
        if migrated != original:
            path.write_text(migrated, encoding="utf-8")
            changed.append(str(path))
    print(f"26.2 structural wave 2 changed {len(changed)} files")
    for path in changed:
        print(path)


if __name__ == "__main__":
    main()
    import structural_26_2_3
    structural_26_2_3.main()
