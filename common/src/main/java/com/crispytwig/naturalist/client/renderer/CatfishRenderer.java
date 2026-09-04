package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.client.model.CatfishModel;
import com.crispytwig.naturalist.server.entity.mob.Catfish;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CatfishRenderer extends NaturalistSingleMobRenderer<Catfish> {
    public CatfishRenderer(EntityRendererProvider.Context context) {
        super(context, new CatfishModel(context.bakeLayer(CatfishModel.LAYER_LOCATION)), 0.0F);
    }
@Override
    protected void setupRotations(@NotNull NaturalistRenderState<Catfish> state, @NotNull PoseStack poseStack, float yBodyRot, float nativeScale) {
        Catfish entity = state.entity;
        float partialTick = state.partialTick;
        super.setupRotations(state, poseStack, yBodyRot, nativeScale);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-entity.swimTilt.getTilt(partialTick)));
    }
}
