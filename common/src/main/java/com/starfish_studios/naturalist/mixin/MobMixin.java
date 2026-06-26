package com.starfish_studios.naturalist.mixin;

import com.starfish_studios.naturalist.NaturalistConfig;
import com.starfish_studios.naturalist.server.entity.mob.Firefly;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {
    protected MobMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
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
