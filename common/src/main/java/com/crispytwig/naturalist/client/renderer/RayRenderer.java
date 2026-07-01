package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.RayModel;
import com.crispytwig.naturalist.server.entity.mob.Ray;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RayRenderer extends GeoEntityRenderer<Ray> {
    public RayRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RayModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public float getMotionAnimThreshold(Ray animatable) {
        return 0.000001f;
    }

    @Override
    public @NotNull RenderType getRenderType(Ray animatable, @NotNull ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
