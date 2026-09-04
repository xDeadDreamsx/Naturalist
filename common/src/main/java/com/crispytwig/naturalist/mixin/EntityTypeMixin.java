package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.variant.LegacyVariantRemap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityType.class)
public class EntityTypeMixin {
    /*
     * 26.2 moved EntityType.by from CompoundTag to ValueInput. LegacyVariantRemap still needs
     * the mutable raw entity tag, so apply it immediately before vanilla wraps that tag in a
     * TagValueInput inside the CompoundTag loadEntityRecursive overload.
     */
    @Inject(
            method = "loadEntityRecursive(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/EntitySpawnRequest;Lnet/minecraft/world/entity/EntityProcessor;)Lnet/minecraft/world/entity/Entity;",
            at = @At("HEAD")
    )
    private static void naturalist$remapLegacyMobs(
            CompoundTag tag,
            Level level,
            EntitySpawnRequest request,
            EntityProcessor postLoad,
            CallbackInfoReturnable<Entity> cir
    ) {
        LegacyVariantRemap.apply(tag);
    }
}
