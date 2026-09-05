package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistAvatarRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Restores Naturalist's 1.21.1 upper-body counter-lean while riding IK mounts. */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
    @Unique
    private static final float naturalist$WAIST_Y = 12.0F;

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void naturalist$counterLeanOnIKMount(AvatarRenderState state, CallbackInfo ci) {
        NaturalistAvatarRenderState naturalistState = (NaturalistAvatarRenderState) state;
        float pitch = -naturalistState.naturalist$ikMountPitch();
        float roll = -naturalistState.naturalist$ikMountRoll();
        if (pitch == 0.0F && roll == 0.0F) {
            return;
        }

        PlayerModel model = (PlayerModel) (Object) this;
        float cosP = (float) Math.cos(pitch);
        float sinP = (float) Math.sin(pitch);
        float cosR = (float) Math.cos(roll);
        float sinR = (float) Math.sin(roll);

        naturalist$rotateWaist(model.head, pitch, roll, cosP, sinP, cosR, sinR);
        naturalist$rotateWaist(model.body, pitch, roll, cosP, sinP, cosR, sinR);
        naturalist$rotateWaist(model.leftArm, pitch, roll, cosP, sinP, cosR, sinR);
        naturalist$rotateWaist(model.rightArm, pitch, roll, cosP, sinP, cosR, sinR);

        model.hat.copyFrom(model.head);
        model.jacket.copyFrom(model.body);
        model.leftSleeve.copyFrom(model.leftArm);
        model.rightSleeve.copyFrom(model.rightArm);
    }

    @Unique
    private static void naturalist$rotateWaist(ModelPart part, float pitch, float roll,
                                                float cosP, float sinP, float cosR, float sinR) {
        float dx = part.x;
        float dy = part.y - naturalist$WAIST_Y;
        float dz = part.z;
        float y1 = dy * cosP - dz * sinP;

        part.x = dx * cosR - y1 * sinR;
        part.y = naturalist$WAIST_Y + dx * sinR + y1 * cosR;
        part.z = dy * sinP + dz * cosP;
        part.xRot += pitch;
        part.zRot += roll;
    }
}
