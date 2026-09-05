package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.client.model.AnglerfishModel;
import com.crispytwig.naturalist.server.entity.mob.Anglerfish;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AnglerfishRenderer extends NaturalistSingleMobRenderer<Anglerfish> {
    public AnglerfishRenderer(EntityRendererProvider.Context context) {
        super(context, new AnglerfishModel(context.bakeLayer(AnglerfishModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    protected int getBlockLightLevel(@NotNull Anglerfish entity, @NotNull BlockPos pos) {
        return entity.isGlowing() ? 15 : super.getBlockLightLevel(entity, pos);
    }

    @Override
    protected int getSkyLightLevel(@NotNull Anglerfish entity, @NotNull BlockPos pos) {
        return entity.isGlowing() ? 15 : super.getSkyLightLevel(entity, pos);
    }

    @Override
    protected void setupRotations(@NotNull NaturalistRenderState<Anglerfish> state, @NotNull PoseStack poseStack, float yBodyRot, float nativeScale) {
        Anglerfish entity = state.entity;
        float partialTick = state.partialTick;
        super.setupRotations(state, poseStack, yBodyRot, nativeScale);
        if (entity != null) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-entity.swimTilt.getTilt(partialTick)));
        }
    }
}
