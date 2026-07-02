package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.AlligatorModel;
import com.crispytwig.naturalist.server.entity.mob.Alligator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AlligatorRenderer extends GeoEntityRenderer<Alligator> {
    public AlligatorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AlligatorModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        this.shadowRadius = 1.0F;
    }

    @Override
    public float getMotionAnimThreshold(Alligator animatable) {
        return 0.000001f;
    }

    @Override
    public void render(Alligator entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.5F : 1.0F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
