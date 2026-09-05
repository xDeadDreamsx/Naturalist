package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.TurkeyModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Turkey;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TurkeyRenderer extends NaturalistSingleMobRenderer<Turkey> {
    public TurkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new TurkeyModel(context.bakeLayer(TurkeyModel.LAYER_LOCATION)), 0.3F);
    }

    @Override
    protected void scale(NaturalistRenderState<Turkey> state, PoseStack poseStack) {
        super.scale(state, poseStack);
        if (state.isBaby) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    protected float getShadowRadius(NaturalistRenderState<Turkey> state) {
        return state.isBaby ? 0.15F : 0.3F;
    }
}
