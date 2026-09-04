package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.TurkeyModel;
import com.crispytwig.naturalist.server.entity.mob.Turkey;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class TurkeyRenderer extends NaturalistSingleMobRenderer<Turkey> {
    public TurkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new TurkeyModel(context.bakeLayer(TurkeyModel.LAYER_LOCATION)), 0.3F);
    }
}
