package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.AnglerfishModel;
import com.crispytwig.naturalist.server.entity.mob.Anglerfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AnglerfishRenderer extends GeoEntityRenderer<Anglerfish> {
    public AnglerfishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AnglerfishModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public float getMotionAnimThreshold(Anglerfish animatable) {
        return 0.000001f;
    }

    @Override
    public void render(Anglerfish entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.isGlowing()) {
            packedLight = LightTexture.FULL_BRIGHT;
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public @NotNull RenderType getRenderType(Anglerfish animatable, @NotNull ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }
}
