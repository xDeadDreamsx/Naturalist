package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.GiraffeModel;
import com.crispytwig.naturalist.server.entity.mob.Giraffe;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GiraffeRenderer extends RideableGeoEntityRenderer<Giraffe> {
    public GiraffeRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GiraffeModel());
        this.shadowRadius = 1.1F;
    }

    @Override
    protected String seatBone() {
        return "seat";
    }

    @Override
    protected float waistPos() {
        return 0.25F;
    }

    @Override
    public void render(Giraffe entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.55F : 1.1F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
