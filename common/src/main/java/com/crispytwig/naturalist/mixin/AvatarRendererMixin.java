package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistAvatarRenderState;
import com.crispytwig.naturalist.server.entity.base.IKMount;
import com.crispytwig.naturalist.server.entity.util.ParrotFlight;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private <T extends Avatar & ClientAvatarEntity> void naturalist$extractNaturalistAvatarState(
            T entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        NaturalistAvatarRenderState naturalistState = (NaturalistAvatarRenderState) state;
        naturalistState.naturalist$setShoulderParrotsFlap(
                ParrotFlight.hasBirdOnHead(entity) && !entity.onGround());

        if (entity.getVehicle() instanceof IKMount mount) {
            naturalistState.naturalist$setSkipNormalIKMountRender(true);
            naturalistState.naturalist$setIKMountPitch(mount.getRenderPitch());
            naturalistState.naturalist$setIKMountRoll(mount.getRenderRoll());
        } else {
            naturalistState.naturalist$setSkipNormalIKMountRender(false);
            naturalistState.naturalist$setIKMountPitch(0.0F);
            naturalistState.naturalist$setIKMountRoll(0.0F);
        }
    }
}
