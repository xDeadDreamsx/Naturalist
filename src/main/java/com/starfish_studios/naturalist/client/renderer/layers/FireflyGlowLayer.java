package com.starfish_studios.naturalist.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Firefly;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

@OnlyIn(Dist.CLIENT)
public class FireflyGlowLayer extends GeoRenderLayer<Firefly> {
    private static final ResourceLocation GLOW = ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/firefly_glow.png");
    private static final ResourceLocation GLOW_E = ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/firefly_glow_e.png");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/firefly.geo.json");
    private static final int TOTAL_FRAMES = 30;
    private static final int TICKS_PER_FRAME = 1;

    public FireflyGlowLayer(GeoRenderer<Firefly> entityRendererIn) {
        super(entityRendererIn);
    }

    @SuppressWarnings("unused")
    @Override
    public void render(PoseStack poseStack, Firefly entity, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTicks, int packedLightIn, int packedOverlay) {
        int frame;
        if (entity.isGlowing()) {
            int elapsed = entity.tickCount - entity.getGlowStartTick();
            frame = Math.min(elapsed / TICKS_PER_FRAME, TOTAL_FRAMES - 1);
        } else {
            frame = 0;
        }

        RenderType baseGlow = RenderType.entityCutoutNoCull(GLOW);
        VertexConsumer baseBuffer = new AnimatedUVVertexConsumer(bufferSource.getBuffer(baseGlow), TOTAL_FRAMES, frame);
        getRenderer().reRender(getDefaultBakedModel(entity), poseStack, bufferSource, entity, baseGlow, baseBuffer, partialTicks, packedLightIn, packedOverlay, -1);

        if (entity.isGlowing()) {
            RenderType emissiveGlow = RenderType.entityTranslucentEmissive(GLOW_E);
            VertexConsumer emissiveBuffer = new AnimatedUVVertexConsumer(bufferSource.getBuffer(emissiveGlow), TOTAL_FRAMES, frame);
            getRenderer().reRender(getDefaultBakedModel(entity), poseStack, bufferSource, entity, emissiveGlow, emissiveBuffer, partialTicks, packedLightIn, packedOverlay, -1);
        }
    }
}
