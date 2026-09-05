package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public class GlowLayer<T extends Entity> extends RenderLayer<NaturalistRenderState<T>, NaturalistEntityModel<T>> {
    private final Function<T, Identifier> glowmask;

    public GlowLayer(RenderLayerParent<NaturalistRenderState<T>, NaturalistEntityModel<T>> parent,
                     Function<T, Identifier> glowmask) {
        super(parent);
        this.glowmask = glowmask;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<T> state, float yRot, float xRot) {
        T entity = state.entity;
        if (entity == null || state.isInvisible) return;
        Identifier texture = this.glowmask.apply(entity);
        if (texture == null) return;
        collector.submitModel(this.getParentModel(), state, poseStack,
                RenderTypes.entityTranslucentEmissive(texture), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
