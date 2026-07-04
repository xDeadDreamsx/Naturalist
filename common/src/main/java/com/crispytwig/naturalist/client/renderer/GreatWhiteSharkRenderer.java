package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.GreatWhiteSharkModel;
import com.crispytwig.naturalist.server.entity.mob.GreatWhiteShark;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GreatWhiteSharkRenderer extends GeoEntityRenderer<GreatWhiteShark> {
    public GreatWhiteSharkRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GreatWhiteSharkModel());
        this.shadowRadius = 0.9F;
    }

    @Override
    public float getMotionAnimThreshold(GreatWhiteShark animatable) {
        return 0.000001f;
    }

    @Override
    protected void applyRotations(GreatWhiteShark animatable, @NotNull PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, animatable.getRenderYaw(partialTick), partialTick, nativeScale);
    }
}
