package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.KomodoDragonModel;
import com.crispytwig.naturalist.server.entity.mob.KomodoDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class KomodoDragonRenderer extends GeoEntityRenderer<KomodoDragon> {
    public KomodoDragonRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new KomodoDragonModel());
        this.shadowRadius = 0.65F;
    }

    @Override
    public void render(@NotNull KomodoDragon entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        this.shadowRadius = entity.isBaby() ? 0.3F : 0.65F;
        if (entity.isBaby()) {
            poseStack.scale(0.45F, 0.45F, 0.45F);
        }
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public float getMotionAnimThreshold(KomodoDragon animatable) {
        return 0.000001f;
    }
}
