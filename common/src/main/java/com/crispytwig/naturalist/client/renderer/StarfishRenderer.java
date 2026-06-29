package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.crispytwig.naturalist.client.model.StarfishModel;
import com.crispytwig.naturalist.server.entity.mob.Starfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class StarfishRenderer extends GeoEntityRenderer<Starfish> {
    public StarfishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new StarfishModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public float getMotionAnimThreshold(Starfish animatable) {
        return 0.000001f;
    }

    public RenderType getRenderType(Starfish entity, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityCutoutNoCull(textureLocation);
    }
}
