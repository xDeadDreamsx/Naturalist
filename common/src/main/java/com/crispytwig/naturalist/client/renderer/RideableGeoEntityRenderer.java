package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtil;

public abstract class RideableGeoEntityRenderer<T extends Mob & GeoAnimatable> extends GeoEntityRenderer<T> {
    protected RideableGeoEntityRenderer(EntityRendererProvider.Context ctx, GeoModel<T> model) {
        super(ctx, model);
    }

    protected abstract String seatBone();

    protected float waistPos() {
        return 0.5F;
    }

    @Override
    public float getMotionAnimThreshold(T animatable) {
        return 0.000001f;
    }

    @Override
    public void renderRecursively(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);

        if (!isReRender && bone.getName().equals(seatBone())) {
            renderBakedRider(poseStack, animatable, bone, bufferSource, partialTick, packedLight);
        }
    }

    private void renderBakedRider(PoseStack poseStack, T mount, GeoBone seat, MultiBufferSource bufferSource, float partialTick, int packedLight) {
        if (!(mount.getFirstPassenger() instanceof Player player)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        EntityRenderer<? super Player> renderer = this.entityRenderDispatcher.getRenderer(player);
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);

        poseStack.pushPose();
        poseStack.translate(seat.getPivotX() / 16f, seat.getPivotY() / 16f, seat.getPivotZ() / 16f);
        RenderUtil.rotateMatrixAroundBone(poseStack, seat);
        poseStack.mulPose(Axis.YP.rotationDegrees(bodyYaw - 180f));
        poseStack.translate(0.0F, -waistPos(), 0.0F);
        renderer.render(player, 0.0F, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
