#!/usr/bin/env python3
"""Restore Rhino charge collision targeting semantics from Naturalist 1.21.1.

The 1.21.1 charge used TargetingConditions.forCombat() when selecting a LivingEntity inside the
Rhino hitbox. An earlier 26.2 migration broadened this to every LivingEntity except the Rhino,
which can select entities the original combat filter would reject and can block a valid target
behind an invalid first result. Minecraft 26.2 still exposes the same combat condition; its test
method now receives ServerLevel explicitly.
"""

from pathlib import Path

RHINO = Path("common/src/main/java/com/crispytwig/naturalist/server/entity/mob/Rhino.java")


def replace_method(text: str, marker: str, replacement: str) -> str:
    if replacement in text:
        return text
    start = text.find(marker)
    if start < 0:
        raise RuntimeError(f"Could not find {marker!r} in {RHINO}")
    brace = text.find("{", start)
    depth = 0
    for i in range(brace, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return text[:start] + replacement + text[i + 1:]
    raise RuntimeError("Unterminated Rhino method")


def main() -> None:
    text = RHINO.read_text(encoding="utf-8")
    original = text

    replacement = """        protected void tryToHurt() {
            if (!(this.mob.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            TargetingConditions combatConditions = TargetingConditions.forCombat();
            List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.mob.getBoundingBox(),
                    entity -> entity != this.mob && combatConditions.test(serverLevel, this.mob, entity));
            if (!nearbyEntities.isEmpty()) {
                LivingEntity livingEntity = nearbyEntities.getFirst();
                if (!(livingEntity instanceof Rhino)) {
                    DamageSource attackSource = livingEntity.damageSources().mobAttack(this.mob);
                    livingEntity.hurtServer(serverLevel, attackSource, (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    float speed = Mth.clamp(this.mob.getSpeed() * 1.65f, 0.2f, 3.0f);
                    float shieldBlockModifier = livingEntity.getItemBlockingWith() != null ? 0.5f : 1.0f;
                    livingEntity.knockback(shieldBlockModifier * speed * 2.0D, this.chargeDirection.x(), this.chargeDirection.z(), attackSource, 0.0F);
                    double knockbackResistance = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(0.0, 0.4f * knockbackResistance, 0.0));
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    if (livingEntity.equals(this.mob.getTarget())) {
                        this.stop();
                    }
                }
            }
        }
"""
    text = replace_method(text, "        protected void tryToHurt() {", replacement)

    if text != original:
        RHINO.write_text(text, encoding="utf-8")
        print(f"26.2 Rhino behavior parity pass changed {RHINO}")
    else:
        print("26.2 Rhino behavior parity pass changed 0 files")


if __name__ == "__main__":
    main()
