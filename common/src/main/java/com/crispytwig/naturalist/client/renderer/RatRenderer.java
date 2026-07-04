package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.RatModel;
import com.crispytwig.naturalist.server.entity.mob.Rat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RatRenderer extends GeoEntityRenderer<Rat> {
    public RatRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RatModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public void render(Rat entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
        }
        this.shadowRadius = entity.isBaby() ? 0.2F : 0.3F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public float getMotionAnimThreshold(Rat animatable) {
        return 0.000001f;
    }
}
