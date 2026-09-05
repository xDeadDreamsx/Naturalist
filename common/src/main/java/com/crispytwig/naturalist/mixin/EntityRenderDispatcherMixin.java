package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistAvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {
    @Inject(method = "submit", at = @At("HEAD"), cancellable = true)
    private void naturalist$skipNormalIKMountRider(EntityRenderState state, CameraRenderState cameraState,
                                                   double x, double y, double z, PoseStack poseStack,
                                                   SubmitNodeCollector collector, CallbackInfo ci) {
        if (state instanceof NaturalistAvatarRenderState naturalistState
                && naturalistState.naturalist$skipNormalIKMountRender()) {
            ci.cancel();
        }
    }
}
