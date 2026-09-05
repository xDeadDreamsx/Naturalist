package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.CrabModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Crab;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class CrabItemLayer extends RenderLayer<NaturalistRenderState<Crab>, CrabModel> {
    private final ItemModelResolver itemModelResolver;
    private final ItemStackRenderState itemState = new ItemStackRenderState();

    public CrabItemLayer(RenderLayerParent<NaturalistRenderState<Crab>, CrabModel> parent, ItemModelResolver itemModelResolver) {
        super(parent);
        this.itemModelResolver = itemModelResolver;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Crab> state, float yRot, float xRot) {
        Crab crab = state.entity;
        if (crab == null) return;
        ItemStack held = crab.getMainHandItem();
        if (held.isEmpty()) return;
        this.itemModelResolver.updateForLiving(this.itemState, held, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, crab);
        poseStack.pushPose();
        this.getParentModel().translateToItem(poseStack);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.itemState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
