package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.crispytwig.naturalist.client.model.ClamModel;
import com.crispytwig.naturalist.client.renderer.layers.ClamItemLayer;
import com.crispytwig.naturalist.server.entity.mob.Clam;
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
public class ClamRenderer extends GeoEntityRenderer<Clam> {
    public ClamRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ClamModel());
        this.shadowRadius = 0.6F;
        this.addRenderLayer(new ClamItemLayer(this));
    }

    public RenderType getRenderType(Clam entity, float partialTicks, PoseStack stack, @Nullable MultiBufferSource renderTypeBuffer, @Nullable VertexConsumer vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityCutoutNoCull(textureLocation);
    }
}
