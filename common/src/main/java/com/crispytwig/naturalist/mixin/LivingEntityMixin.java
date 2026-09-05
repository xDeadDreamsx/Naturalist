package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.mob.Rhino;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores Naturalist 1.21.1's attacker-side shield callback for Rhino.
 * Minecraft 26.2 performs item blocking from the defender; this hook runs after the
 * defender's blockUsingItem path and forwards the event to the attacking Rhino.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "blockUsingItem", at = @At("TAIL"))
    private void naturalist$notifyRhinoOfBlockedAttack(
            ServerLevel level,
            LivingEntity attacker,
            DamageSource source,
            float amount,
            CallbackInfo ci) {
        if (attacker instanceof Rhino rhino) {
            rhino.naturalist$onAttackBlocked((LivingEntity) (Object) this);
        }
    }
}
