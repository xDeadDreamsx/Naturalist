package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistWolfRenderState;
import com.crispytwig.naturalist.server.entity.base.WolfMoleDigging;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.world.entity.animal.wolf.Wolf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfRenderer.class)
public abstract class WolfRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void naturalist$extractMoleDigging(Wolf entity, WolfRenderState state, float partialTicks, CallbackInfo ci) {
        ((NaturalistWolfRenderState) state).naturalist$setDiggingOutMole(
                ((WolfMoleDigging) entity).naturalist$isDiggingOutMole());
    }
}
