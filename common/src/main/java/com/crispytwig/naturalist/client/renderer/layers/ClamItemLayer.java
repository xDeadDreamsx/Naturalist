package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.ClamModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Clam;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;

public class ClamItemLayer extends RenderLayer<NaturalistRenderState<Clam>, NaturalistEntityModel<Clam>> {
    private final ItemModelResolver itemModelResolver;
    private final EntityRenderDispatcher dispatcher;
    private final ItemStackRenderState itemState = new ItemStackRenderState();
    private final Quaternionf scratchRotation = new Quaternionf();

    public ClamItemLayer(RenderLayerParent<NaturalistRenderState<Clam>, NaturalistEntityModel<Clam>> parent,
                         ItemModelResolver itemModelResolver, EntityRenderDispatcher dispatcher) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
        this.dispatcher = dispatcher;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Clam> state, float yRot, float xRot) {
        Clam clam = state.entity;
        if (clam == null) return;
        ItemStack held = clam.getMainHandItem();
        if (held.isEmpty() || !(this.getParentModel() instanceof ClamModel model)) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.FIXED, clam);
        poseStack.pushPose();
        model.translateToItem(poseStack);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        poseStack.translate(0.0F, 0.6F + Mth.sin((clam.tickCount + state.partialTick) * 0.1F) * 0.2F, 0.0F);
        poseStack.mulPose(poseStack.last().pose().getNormalizedRotation(this.scratchRotation).conjugate());
        if (this.dispatcher.camera != null) {
            poseStack.mulPose(this.dispatcher.camera.rotation());
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
