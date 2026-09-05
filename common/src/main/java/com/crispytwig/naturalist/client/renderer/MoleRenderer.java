package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.MoleModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Mole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MoleRenderer extends NaturalistSingleMobRenderer<Mole> {
    public MoleRenderer(EntityRendererProvider.Context context) {
        super(context, new MoleModel(context.bakeLayer(MoleModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    protected void scale(NaturalistRenderState<Mole> state, PoseStack poseStack) {
        super.scale(state, poseStack);
        if (state.isBaby) {
            poseStack.scale(0.6F, 0.6F, 0.6F);
        }
    }

    @Override
    protected float getShadowRadius(NaturalistRenderState<Mole> state) {
        Mole entity = state.entity;
        if (entity != null && entity.isRolledUp()) return 0.0F;
        return state.isBaby ? 0.25F : 0.4F;
    }
}
