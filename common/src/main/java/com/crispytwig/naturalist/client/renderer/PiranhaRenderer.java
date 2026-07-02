package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.PiranhaModel;
import com.crispytwig.naturalist.server.entity.mob.Piranha;
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
public class PiranhaRenderer extends GeoEntityRenderer<Piranha> {
    public PiranhaRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PiranhaModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public float getMotionAnimThreshold(Piranha animatable) {
        return 0.000001f;
    }

    @Override
    public @NotNull RenderType getRenderType(Piranha animatable, @NotNull ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
