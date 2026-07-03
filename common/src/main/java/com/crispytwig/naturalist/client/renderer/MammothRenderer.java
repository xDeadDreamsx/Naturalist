package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.MammothModel;
import com.crispytwig.naturalist.server.entity.mob.Elephant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MammothRenderer extends RideableGeoEntityRenderer<Elephant> {
    public MammothRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MammothModel());
        this.shadowRadius = 1.5F;
    }

    @Override
    protected String seatBone() {
        return "seat";
    }

    @Override
    public void render(Elephant entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.75F : 1.5F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
