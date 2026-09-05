package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.KomodoDragonModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.KomodoDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class KomodoDragonRenderer extends NaturalistSingleMobRenderer<KomodoDragon> {
    public KomodoDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new KomodoDragonModel(context.bakeLayer(KomodoDragonModel.LAYER_LOCATION)), 0.65F);
    }

    @Override
    protected void scale(@NotNull NaturalistRenderState<KomodoDragon> state, @NotNull PoseStack poseStack) {
        if (state.entity != null && state.entity.isBaby()) {
            poseStack.scale(0.45F, 0.45F, 0.45F);
        }
    }

    @Override
    protected float getShadowRadius(@NotNull NaturalistRenderState<KomodoDragon> state) {
        return state.entity != null && state.entity.isBaby() ? 0.3F : 0.65F;
    }
}
