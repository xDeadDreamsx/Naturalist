package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.base.IKMount;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin {
    @Unique
    private static final float naturalist$waistY = 12.0F;
    @Unique
    private static final float naturalist$pitchSign = -1.0F;
    @Unique
    private static final float naturalist$rollSign = -1.0F;

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightArm;

    @Unique
    private boolean naturalist$leanApplied;

    @Inject(method = "setupAnim*", at = @At("HEAD"))
    private void naturalist$resetLean(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (naturalist$leanApplied || naturalist$leanMount(entity) != null) {
            this.head.resetPose();
            this.body.resetPose();
            this.leftArm.resetPose();
            this.rightArm.resetPose();
            this.naturalist$leanApplied = false;
        }
    }

    @Inject(method = "setupAnim*", at = @At("TAIL"))
    private void naturalist$counterLean(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        IKMount mount = naturalist$leanMount(entity);
        if (mount == null) {
            return;
        }
        float pitch = mount.getRenderPitch() * naturalist$pitchSign;
        float roll = mount.getRenderRoll() * naturalist$rollSign;
        if (pitch == 0.0F && roll == 0.0F) {
            return;
        }
        float cosP = (float) Math.cos(pitch), sinP = (float) Math.sin(pitch);
        float cosR = (float) Math.cos(roll), sinR = (float) Math.sin(roll);
        naturalist$rotateWaist(this.head, pitch, roll, cosP, sinP, cosR, sinR);
        naturalist$rotateWaist(this.body, pitch, roll, cosP, sinP, cosR, sinR);
        naturalist$rotateWaist(this.leftArm, pitch, roll, cosP, sinP, cosR, sinR);
        naturalist$rotateWaist(this.rightArm, pitch, roll, cosP, sinP, cosR, sinR);
        this.naturalist$leanApplied = true;
    }

    @Unique
    private static IKMount naturalist$leanMount(LivingEntity entity) {
        if (entity instanceof Player && entity.getVehicle() instanceof IKMount mount) {
            return mount;
        }
        return null;
    }

    @Unique
    private static void naturalist$rotateWaist(ModelPart part, float pitch, float roll, float cosP, float sinP, float cosR, float sinR) {
        PartPose base = part.getInitialPose();
        float dx = base.x;
        float dy = base.y - naturalist$waistY;
        float dz = base.z;

        float y1 = dy * cosP - dz * sinP;
        float z1 = dy * sinP + dz * cosP;

        float x2 = dx * cosR - y1 * sinR;
        float y2 = dx * sinR + y1 * cosR;

        part.x = x2;
        part.y = naturalist$waistY + y2;
        part.z = z1;
        part.xRot += pitch;
        part.zRot += roll;
    }
}
