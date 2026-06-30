package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.JellyfishModel;
import com.crispytwig.naturalist.server.entity.mob.Jellyfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class JellyfishRenderer extends GeoEntityRenderer<Jellyfish> {
    public JellyfishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new JellyfishModel());
        this.shadowRadius = 0.5F;
    }

    @Override
    public float getMotionAnimThreshold(Jellyfish animatable) {
        return 0.000001f;
    }

    @Override
    public @NotNull RenderType getRenderType(Jellyfish animatable, @NotNull ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public @NotNull Color getRenderColor(Jellyfish animatable, float partialTick, int packedLight) {
        return Color.ofRGBA(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
