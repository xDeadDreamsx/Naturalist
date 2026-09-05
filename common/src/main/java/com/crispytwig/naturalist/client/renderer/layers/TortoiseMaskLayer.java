package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Tortoise;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public class TortoiseMaskLayer extends RenderLayer<NaturalistRenderState<Tortoise>, NaturalistEntityModel<Tortoise>> {
    private static final Identifier DONATELLO = Naturalist.location("textures/entity/tortoise/donatello.png");
    private static final Identifier LEONARDO = Naturalist.location("textures/entity/tortoise/leonardo.png");
    private static final Identifier MICHELANGELO = Naturalist.location("textures/entity/tortoise/michelangelo.png");
    private static final Identifier RAPHAEL = Naturalist.location("textures/entity/tortoise/raphael.png");

    public TortoiseMaskLayer(RenderLayerParent<NaturalistRenderState<Tortoise>, NaturalistEntityModel<Tortoise>> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
                       NaturalistRenderState<Tortoise> state, float yRot, float xRot) {
        Tortoise entity = state.entity;
        if (entity == null || state.isInvisible || !entity.hasCustomName()) return;
        Identifier skin = switch (entity.getName().getString()) {
            case "Donatello" -> DONATELLO;
            case "Leonardo" -> LEONARDO;
            case "Michelangelo" -> MICHELANGELO;
            case "Raphael" -> RAPHAEL;
            default -> null;
        };
        if (skin == null) return;
        collector.submitModel(this.getParentModel(), state, poseStack, RenderTypes.entityCutout(skin), lightCoords,
                LivingEntityRenderer.getOverlayCoords(state, 0.0F), -1, null, state.outlineColor, null);
    }
}
