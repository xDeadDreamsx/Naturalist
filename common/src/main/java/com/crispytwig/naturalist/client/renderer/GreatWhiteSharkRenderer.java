package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.client.model.GreatWhiteSharkModel;
import com.crispytwig.naturalist.server.entity.mob.GreatWhiteShark;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class GreatWhiteSharkRenderer extends NaturalistSingleMobRenderer<GreatWhiteShark> {
    public GreatWhiteSharkRenderer(EntityRendererProvider.Context context) {
        super(context, new GreatWhiteSharkModel(context.bakeLayer(GreatWhiteSharkModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    protected void setupRotations(@NotNull NaturalistRenderState<GreatWhiteShark> state, @NotNull PoseStack poseStack, float yBodyRot, float nativeScale) {
        GreatWhiteShark entity = state.entity;
        float renderYaw = entity != null ? entity.getRenderYaw(state.partialTick) : yBodyRot;
        super.setupRotations(state, poseStack, renderYaw, nativeScale);
    }
}
