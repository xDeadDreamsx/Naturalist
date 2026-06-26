package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.crispytwig.naturalist.client.model.FireflyModel;
import com.crispytwig.naturalist.client.renderer.layers.FireflyGlowLayer;
import com.crispytwig.naturalist.server.entity.mob.Firefly;
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
public class FireflyRenderer extends GeoEntityRenderer<Firefly> {
    public FireflyRenderer(EntityRendererProvider.@NotNull Context renderManager) {
        super(renderManager, new FireflyModel());
        this.shadowRadius = 0.4F;
        this.addRenderLayer(new FireflyGlowLayer(this));
    }

    @Override
    public float getMotionAnimThreshold(Firefly animatable) {
        return 0.000001f;
    }

    @Override
    public void render(Firefly entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.2F : 0.4F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

   public RenderType getRenderType(Firefly entity, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, @NotNull ResourceLocation textureLocation) {
        return RenderType.entityCutoutNoCull(textureLocation);
    }
}
