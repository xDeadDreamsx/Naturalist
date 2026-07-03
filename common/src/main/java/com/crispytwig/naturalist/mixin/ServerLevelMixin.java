package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.mob.WhalePart;
import com.crispytwig.naturalist.server.entity.util.MultipartLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Inject(method = "getEntityOrPart(I)Lnet/minecraft/world/entity/Entity;",
            at = @At("RETURN"),
            cancellable = true)
    private void naturalist$resolveWhalePart(int id, CallbackInfoReturnable<Entity> cir) {
        if (cir.getReturnValue() == null && (Object) this instanceof MultipartLevel multipart) {
            WhalePart part = multipart.naturalist$getWhalePart(id);
            if (part != null) {
                cir.setReturnValue(part);
            }
        }
    }
}
