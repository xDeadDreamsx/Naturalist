package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.TurkeyModel;
import com.crispytwig.naturalist.server.entity.mob.Turkey;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TurkeyRenderer extends GeoEntityRenderer<Turkey> {
    public TurkeyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TurkeyModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(@NotNull Turkey entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.15F : 0.3F;
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public float getMotionAnimThreshold(Turkey animatable) {
        return 0.000001f;
    }
}
