package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.WhaleModel;
import com.crispytwig.naturalist.server.entity.mob.Whale;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class WhaleRenderer extends GeoEntityRenderer<Whale> {
    public WhaleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WhaleModel());
        this.shadowRadius = 1.5F;
    }

    @Override
    public float getMotionAnimThreshold(Whale animatable) {
        return 0.000001f;
    }

    @Override
    protected void applyRotations(Whale animatable, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, animatable.getRenderYaw(partialTick), partialTick, nativeScale);
    }

    @Override
    public @NotNull RenderType getRenderType(Whale animatable, @NotNull ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
