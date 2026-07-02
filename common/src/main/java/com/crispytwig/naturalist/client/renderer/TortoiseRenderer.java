package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.TortoiseModel;
import com.crispytwig.naturalist.client.renderer.layers.TortoiseSkinLayer;
import com.crispytwig.naturalist.server.entity.mob.Tortoise;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TortoiseRenderer extends GeoEntityRenderer<Tortoise> {
    public TortoiseRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new TortoiseModel());
        this.shadowRadius = 0.8F;
        this.addRenderLayer(new TortoiseSkinLayer(this));
    }

    @Override
    public float getMotionAnimThreshold(Tortoise animatable) {
        return 0.000001f;
    }

    @Override
    public void render(Tortoise entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.4F : 0.8F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
