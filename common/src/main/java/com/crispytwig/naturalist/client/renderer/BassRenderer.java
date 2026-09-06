package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BassModel;
import com.crispytwig.naturalist.client.model.LargeBassModel;
import com.crispytwig.naturalist.client.model.MediumBassModel;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Bass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BassRenderer extends NaturalistSingleMobRenderer<Bass> {
    private final NaturalistEntityModel<Bass> normalModel;
    private final NaturalistEntityModel<Bass> mediumModel;
    private final NaturalistEntityModel<Bass> largeModel;

    public BassRenderer(EntityRendererProvider.Context context) {
        super(context, new BassModel(context.bakeLayer(BassModel.LAYER_LOCATION)), 0.0F);
        this.normalModel = this.model;
        this.mediumModel = new MediumBassModel(context.bakeLayer(MediumBassModel.LAYER_LOCATION));
        this.largeModel = new LargeBassModel(context.bakeLayer(LargeBassModel.LAYER_LOCATION));
    }

    @Override
    public void submit(NaturalistRenderState<Bass> state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        Bass entity = state.entity;
        if (entity != null && entity.isLargeVariant()) {
            this.model = this.largeModel;
        } else if (entity != null && entity.isMediumVariant()) {
            this.model = this.mediumModel;
        } else {
            this.model = this.normalModel;
        }
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    @Override
    protected void setupRotations(@NotNull NaturalistRenderState<Bass> state, @NotNull PoseStack poseStack, float yBodyRot, float nativeScale) {
        Bass entity = state.entity;
        float partialTick = state.partialTick;
        super.setupRotations(state, poseStack, yBodyRot, nativeScale);
        if (entity != null) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(-entity.swimTilt.getTilt(partialTick)));
        }
    }
}
