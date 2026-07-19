package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.NaturalistConfig;
import com.crispytwig.naturalist.server.entity.mob.Firefly;
import com.crispytwig.naturalist.registry.NaturalistTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    @Unique
    private static final ResourceLocation naturalist$BABY_HEALTH_ID = ResourceLocation.fromNamespaceAndPath("naturalist", "baby_health");
    @Unique
    private static final double naturalist$BABY_HEALTH_SCALE = 1.70D;
    @Unique
    private static final double naturalist$BABY_HEALTH_MAX_RATIO = 0.75D;
    @Unique
    private static final double naturalist$BABY_HEALTH_STEP = 2.0D;
    @Unique
    private static final double naturalist$BABY_HEALTH_EPSILON = 1.0E-4D;

    @Unique
    private Boolean naturalist$wasBaby;

    @Unique
    private static double naturalist$babyMaxHealth(double adultMax) {
        double raw = Math.min(naturalist$BABY_HEALTH_SCALE * Math.sqrt(adultMax), adultMax * naturalist$BABY_HEALTH_MAX_RATIO);
        double stepped = Math.round(raw / naturalist$BABY_HEALTH_STEP) * naturalist$BABY_HEALTH_STEP;
        double ceiling = Math.max(naturalist$BABY_HEALTH_STEP,
                Math.floor((adultMax - naturalist$BABY_HEALTH_EPSILON) / naturalist$BABY_HEALTH_STEP) * naturalist$BABY_HEALTH_STEP);
        return Math.clamp(stepped, naturalist$BABY_HEALTH_STEP, ceiling);
    }

    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "aiStep", at = @At("TAIL"))
    private void naturalist$updateBabyHealth(CallbackInfo ci) {
        if (this.level().isClientSide || !this.getType().is(NaturalistTags.EntityTypes.NATURALIST_ENTITIES)) {
            return;
        }

        boolean baby = this.isBaby();
        if (this.naturalist$wasBaby != null && this.naturalist$wasBaby == baby) {
            return;
        }
        this.naturalist$wasBaby = baby;

        AttributeInstance maxHealth = this.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        AttributeModifier existing = maxHealth.getModifier(naturalist$BABY_HEALTH_ID);
        if (this.isBaby()) {
            double adultMax = existing == null ? this.getMaxHealth() : this.getMaxHealth() / (1.0D + existing.amount());
            if (adultMax <= 0.0D) {
                return;
            }
            double amount = naturalist$babyMaxHealth(adultMax) / adultMax - 1.0D;
            if (existing != null && Math.abs(existing.amount() - amount) < naturalist$BABY_HEALTH_EPSILON) {
                return;
            }
            if (existing != null) {
                maxHealth.removeModifier(naturalist$BABY_HEALTH_ID);
            }
            maxHealth.addTransientModifier(new AttributeModifier(naturalist$BABY_HEALTH_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            this.setHealth(Math.min(this.getHealth(), this.getMaxHealth()));
        } else if (existing != null) {
            float healthFraction = this.getMaxHealth() > 0.0F ? this.getHealth() / this.getMaxHealth() : 1.0F;
            maxHealth.removeModifier(naturalist$BABY_HEALTH_ID);
            this.setHealth(this.getMaxHealth() * healthFraction);
        }
    }

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void naturalist$clearSwingLatch(CallbackInfo ci) {
        if (this.getType().is(NaturalistTags.EntityTypes.NATURALIST_ENTITIES)) {
            this.updateSwingTime();
        }
    }

    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    @SuppressWarnings("unused")
    private void naturalist$onDoHurtTarget(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).equals(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.FROG))
                && target instanceof Firefly) {
            this.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60));
        }
    }

    @Inject(method = "checkDespawn", at = @At("HEAD"), cancellable = true)
    private void naturalist$checkDespawnMixin(CallbackInfo ci) {
        if (!this.getType().is(NaturalistTags.EntityTypes.NATURALIST_ENTITIES)) {
            return;
        }

        if (BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath().equals("caterpillar")) {
            return;
        }

        String mobName = naturalist$toCamelCase(BuiltInRegistries.ENTITY_TYPE.getKey(this.getType()).getPath());

        if (NaturalistConfig.isRemoved(mobName)) {
            this.remove(Entity.RemovalReason.DISCARDED);
            ci.cancel();
        }
    }

    @Unique
    private String naturalist$toCamelCase(String input) {
        StringBuilder camelCase = new StringBuilder();
        boolean capitalizeNext = false;

        for (char c : input.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                camelCase.append(capitalizeNext ? Character.toUpperCase(c) : c);
                capitalizeNext = false;
            }
        }

        return camelCase.toString();
    }
}
