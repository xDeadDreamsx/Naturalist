package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.base.DyeableAnimal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class DyeOverlayLayer<T extends LivingEntity & GeoAnimatable & DyeableAnimal> extends GeoRenderLayer<T> {
    private final String folder;

    public DyeOverlayLayer(GeoRenderer<T> renderer, String folder) {
        super(renderer);
        this.folder = folder;
    }

    @Override
    public void render(PoseStack poseStack, T entity, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLightIn, int packedOverlay) {
        DyeColor color = entity.getDyeColor();
        if (color == null) {
            return;
        }
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/" + this.folder + "/dye/" + color.getName() + ".png");
        RenderType renderLayer = RenderType.entityCutoutNoCull(texture);
        getRenderer().reRender(getDefaultBakedModel(entity), poseStack, bufferSource, entity, renderLayer, bufferSource.getBuffer(renderLayer), partialTick, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
    }
}
