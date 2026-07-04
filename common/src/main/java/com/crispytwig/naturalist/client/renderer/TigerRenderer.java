package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.TigerModel;
import com.crispytwig.naturalist.server.entity.mob.Tiger;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TigerRenderer extends GeoEntityRenderer<Tiger> {
    public TigerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TigerModel());
        this.shadowRadius = 1.1F;
    }

    @Override
    public void render(@NotNull Tiger entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.55F : 1.1F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public float getMotionAnimThreshold(Tiger animatable) {
        return 0.000001f;
    }
}
