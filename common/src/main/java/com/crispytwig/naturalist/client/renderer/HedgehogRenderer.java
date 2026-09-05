package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.client.model.HedgehogModel;
import com.crispytwig.naturalist.server.entity.mob.Hedgehog;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import com.crispytwig.naturalist.client.renderer.layers.DyeLayer;
import com.crispytwig.naturalist.client.renderer.layers.HedgehogGlintLayer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class HedgehogRenderer extends NaturalistSingleMobRenderer<Hedgehog> {
    public HedgehogRenderer(EntityRendererProvider.Context context) {
        super(context, new HedgehogModel(context.bakeLayer(HedgehogModel.LAYER_LOCATION)), 0.25F);
        this.addLayer(new DyeLayer<>(this, "hedgehog"));
        this.addLayer(new HedgehogGlintLayer(this));
    }
@Override
    protected void scale(@NotNull NaturalistRenderState<Hedgehog> state, @NotNull PoseStack poseStack) {
        Hedgehog entity = state.entity;
        if (entity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }
}
