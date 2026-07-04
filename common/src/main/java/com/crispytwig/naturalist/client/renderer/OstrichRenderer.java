package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.OstrichModel;
import com.crispytwig.naturalist.client.renderer.layers.OstrichDyeOverlayLayer;
import com.crispytwig.naturalist.server.entity.mob.Ostrich;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class OstrichRenderer extends RideableGeoEntityRenderer<Ostrich> {
    public OstrichRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new OstrichModel());
        this.shadowRadius = 0.7F;
        this.addRenderLayer(new OstrichDyeOverlayLayer(this));
    }

    @Override
    protected String seatBone() {
        return "seat";
    }

    @Override
    public void render(@NotNull Ostrich entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.35F : 0.7F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
