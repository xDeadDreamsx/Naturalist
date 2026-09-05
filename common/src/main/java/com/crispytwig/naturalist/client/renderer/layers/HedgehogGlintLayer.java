package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Hedgehog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public class HedgehogGlintLayer extends RenderLayer<NaturalistRenderState<Hedgehog>, NaturalistEntityModel<Hedgehog>> {
    public HedgehogGlintLayer(RenderLayerParent<NaturalistRenderState<Hedgehog>, NaturalistEntityModel<Hedgehog>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Hedgehog> state, float yRot, float xRot) {
        Hedgehog entity = state.entity;
        if (entity == null || state.isInvisible || !entity.hasThrowEnchantments()) return;
        collector.submitModel(this.getParentModel(), state, poseStack, RenderTypes.entityGlint(), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
