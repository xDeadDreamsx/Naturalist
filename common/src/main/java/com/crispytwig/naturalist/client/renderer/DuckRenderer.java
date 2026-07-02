package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.DuckModel;
import com.crispytwig.naturalist.client.renderer.layers.DyeOverlayLayer;
import com.crispytwig.naturalist.server.entity.mob.Duck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DuckRenderer extends GeoEntityRenderer<Duck> {
    public DuckRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DuckModel());
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new DyeOverlayLayer<>(this, "duck"));
    }

    @Override
    public float getMotionAnimThreshold(Duck animatable) {
        return 0.000001f;
    }

    @Override
    public void render(Duck entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.15F : 0.3F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
