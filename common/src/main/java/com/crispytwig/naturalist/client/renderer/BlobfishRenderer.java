package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BlobfishModel;
import com.crispytwig.naturalist.server.entity.mob.Blobfish;
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
public class BlobfishRenderer extends GeoEntityRenderer<Blobfish> {
    public BlobfishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BlobfishModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public float getMotionAnimThreshold(Blobfish animatable) {
        return 0.000001f;
    }

    @Override
    public @NotNull RenderType getRenderType(Blobfish animatable, @NotNull ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
