package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.DirtTrailModel;
import com.crispytwig.naturalist.server.entity.misc.DirtTrail;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DirtTrailRenderer extends GeoEntityRenderer<DirtTrail> {
    public DirtTrailRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DirtTrailModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DirtTrail entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        poseStack.translate(0.0D, -(entity.getId() % 3) * 0.0625D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.getId() * 61) % 360));
        if (entity.isSmall()) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
