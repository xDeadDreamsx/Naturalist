package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.client.model.RatModel;
import com.crispytwig.naturalist.server.entity.mob.Rat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RatRenderer extends NaturalistSingleMobRenderer<Rat> {
    public RatRenderer(EntityRendererProvider.Context context) {
        super(context, new RatModel(context.bakeLayer(RatModel.LAYER_LOCATION)), 0.3F);
    }
@Override
    protected void scale(@NotNull NaturalistRenderState<Rat> state, @NotNull PoseStack poseStack) {
        Rat entity = state.entity;
        if (entity.isBaby()) {
            poseStack.scale(0.75F, 0.75F, 0.75F);
        }
    }
}
