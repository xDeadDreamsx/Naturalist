package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.GreatWhiteSharkModel;
import com.crispytwig.naturalist.server.entity.mob.GreatWhiteShark;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GreatWhiteSharkRenderer extends MobRenderer<GreatWhiteShark, HierarchicalModel<GreatWhiteShark>> {
    public GreatWhiteSharkRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatWhiteSharkModel(context.bakeLayer(GreatWhiteSharkModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GreatWhiteShark entity) {
        return entity.getVariantTexture();
    }

    @Override
    protected void setupRotations(@NotNull GreatWhiteShark entity, @NotNull PoseStack poseStack, float bob, float yBodyRot, float partialTick, float nativeScale) {
        super.setupRotations(entity, poseStack, bob, entity.getRenderYaw(partialTick), partialTick, nativeScale);
    }
}
