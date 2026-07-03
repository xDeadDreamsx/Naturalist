package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.MoleModel;
import com.crispytwig.naturalist.server.entity.mob.Mole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MoleRenderer extends GeoEntityRenderer<Mole> {
    public MoleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MoleModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public void render(Mole entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        }
        this.shadowRadius = entity.isRolledUp() ? 0.0F : entity.isBaby() ? 0.25F : 0.4F;
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public float getMotionAnimThreshold(Mole animatable) {
        return 0.000001f;
    }
}
