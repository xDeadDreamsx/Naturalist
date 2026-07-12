package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.SnailModel;
import com.crispytwig.naturalist.server.entity.mob.Snail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SnailRenderer extends GeoEntityRenderer<Snail> {
    public SnailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SnailModel());
        this.shadowRadius = 0.2F;
    }

    @Override
    public float getMotionAnimThreshold(Snail animatable) {
        return 0.000001f;
    }

    @Override
    protected void applyRotations(Snail animatable, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        Vec3 normal = animatable.getClimbing().getRenderNormal(partialTick);
        Vec3 back = animatable.getClimbing().getRenderForwardFlattened(partialTick, normal).scale(-1.0D);
        Vec3 right = normal.cross(back);
        Vec3 anchor = animatable.getClimbing().getRenderAnchor(partialTick);

        poseStack.translate(anchor.x, anchor.y, anchor.z);
        poseStack.mulPose(new Matrix4f(
                (float) right.x, (float) right.y, (float) right.z, 0.0F,
                (float) normal.x, (float) normal.y, (float) normal.z, 0.0F,
                (float) back.x, (float) back.y, (float) back.z, 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F));

        if (animatable.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }

        super.applyRotations(animatable, poseStack, ageInTicks, 180.0F, partialTick, nativeScale);
    }
}
