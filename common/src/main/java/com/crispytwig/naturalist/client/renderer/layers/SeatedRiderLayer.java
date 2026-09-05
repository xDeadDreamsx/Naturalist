package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.model.SeatedModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class SeatedRiderLayer<T extends Mob & DataDrivenVariantAnimal>
        extends RenderLayer<NaturalistRenderState<T>, NaturalistEntityModel<T>> {
    private final EntityRenderDispatcher dispatcher;

    public SeatedRiderLayer(RenderLayerParent<NaturalistRenderState<T>, NaturalistEntityModel<T>> parent,
                            EntityRenderDispatcher dispatcher) {
        super(parent);
        this.dispatcher = dispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<T> state, float yRot, float xRot) {
        T entity = state.entity;
        if (entity == null || !(entity.getFirstPassenger() instanceof Player player)
                || !(this.getParentModel() instanceof SeatedModel seatedModel)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player && minecraft.options.getCameraType().isFirstPerson()) return;

        EntityRenderState riderState = this.dispatcher.extractEntity(player, state.partialTick);
        // The normal passenger render already owns name tags and shadows. This submission exists only
        // to follow Naturalist's animated seat transform, matching the 1.21.1 SeatedRiderLayer.
        riderState.nameTag = null;
        riderState.scoreText = null;
        riderState.shadowPieces.clear();
        riderState.shadowRadius = 0.0F;

        CameraRenderState cameraState = new CameraRenderState();
        minecraft.gameRenderer.getMainCamera().extractRenderState(cameraState, state.partialTick);

        poseStack.pushPose();
        seatedModel.translateToSeat(poseStack);
        poseStack.mulPose(Axis.ZP.rotationDegrees(-seatedModel.seatZRot() * 0.1F * (float)(180.0 / Math.PI)));
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.bodyRot - 180.0F));
        poseStack.translate(0.0F, -seatedModel.seatHeight(), 0.0F);
        this.dispatcher.submit(riderState, cameraState, 0.0, 0.0, 0.0, poseStack, collector);
        poseStack.popPose();
    }
}
