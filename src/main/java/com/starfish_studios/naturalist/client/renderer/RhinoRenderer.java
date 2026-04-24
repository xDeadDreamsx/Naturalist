package com.starfish_studios.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfish_studios.naturalist.client.model.RhinoModel;
import com.starfish_studios.naturalist.server.entity.mob.Rhino;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class RhinoRenderer extends GeoEntityRenderer<Rhino> {
    public RhinoRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RhinoModel());
        this.shadowRadius = 1.1F;
    }

    @Override
    public float getMotionAnimThreshold(Rhino animatable) {
        return 0.000001f;
    }

    @Override
    public void render(Rhino entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }else{
            poseStack.scale(0.9F, 0.9F, 0.9F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

   public @NotNull RenderType getRenderType(Rhino entity, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityCutoutNoCull(textureLocation);
    }
}
