package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.DirtTrailModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.misc.DirtTrail;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class DirtTrailRenderer extends EntityRenderer<DirtTrail, NaturalistRenderState<DirtTrail>> {
    private static final Identifier TEXTURE = Naturalist.location("textures/entity/mole.png");
    private final DirtTrailModel model;

    public DirtTrailRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DirtTrailModel(context.bakeLayer(DirtTrailModel.LAYER_LOCATION));
        this.shadowRadius = 0.0F;
    }

    @Override
    public NaturalistRenderState<DirtTrail> createRenderState() {
        return new NaturalistRenderState<>();
    }

    @Override
    public void extractRenderState(DirtTrail entity, NaturalistRenderState<DirtTrail> state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
        state.ageInTicks = entity.tickCount + partialTick;
    }

    @Override
    public void submit(NaturalistRenderState<DirtTrail> state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        DirtTrail entity = state.entity;
        if (entity == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.0F, -(entity.getId() % 3) * 0.0625F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.getId() * 61) % 360));
        if (entity.isSmall()) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, 0.01F, 0.0F);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -1.5F, 0.0F);

        this.model.setupAnim(state);
        collector.submitModel(this.model, state, poseStack, RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, 0, -1, null, state.outlineColor, null);
        poseStack.popPose();

        super.submit(state, poseStack, collector, cameraState);
    }
}
