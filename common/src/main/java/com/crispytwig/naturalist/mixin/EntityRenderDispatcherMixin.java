package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.server.entity.base.IKMount;
import com.crispytwig.naturalist.server.entity.mob.Whale;
import com.crispytwig.naturalist.server.entity.mob.WhalePart;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void naturalist$skipBakedRider(Entity entity, double x, double y, double z, float rotationYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        if (entity instanceof Player && entity.getVehicle() instanceof IKMount) {
            ci.cancel();
        }
    }

    @Inject(method = "renderHitbox", at = @At("TAIL"))
    private static void naturalist$renderWhalePartHitboxes(PoseStack poseStack, VertexConsumer buffer, Entity entity, float partialTicks, float red, float green, float blue, CallbackInfo ci) {
        if (entity instanceof Whale whale) {
            double x = -Mth.lerp(partialTicks, whale.xOld, whale.getX());
            double y = -Mth.lerp(partialTicks, whale.yOld, whale.getY());
            double z = -Mth.lerp(partialTicks, whale.zOld, whale.getZ());
            for (WhalePart part : whale.getParts()) {
                poseStack.pushPose();
                double px = x + Mth.lerp(partialTicks, part.xOld, part.getX());
                double py = y + Mth.lerp(partialTicks, part.yOld, part.getY());
                double pz = z + Mth.lerp(partialTicks, part.zOld, part.getZ());
                poseStack.translate(px, py, pz);
                LevelRenderer.renderLineBox(poseStack, buffer, part.getBoundingBox().move(-part.getX(), -part.getY(), -part.getZ()), 0.25F, 1.0F, 0.0F, 1.0F);
                poseStack.popPose();
            }
        }
    }
}
