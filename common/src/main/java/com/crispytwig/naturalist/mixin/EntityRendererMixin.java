package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistAvatarRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void naturalist$skipNormalIKMountRider(EntityRenderState state, PoseStack poseStack,
                                                   SubmitNodeCollector collector, CameraRenderState cameraState,
                                                   CallbackInfo ci) {
        if (state instanceof NaturalistAvatarRenderState naturalistState
                && naturalistState.naturalist$skipNormalIKMountRender()) {
            ci.cancel();
        }
    }
}
