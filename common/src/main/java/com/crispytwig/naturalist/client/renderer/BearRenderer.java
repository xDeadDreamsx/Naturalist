package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BearBabyModel;
import com.crispytwig.naturalist.client.model.BearModel;
import com.crispytwig.naturalist.server.entity.mob.Bear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BearRenderer extends NaturalistMobRenderer<Bear> {
    public BearRenderer(EntityRendererProvider.Context context) {
        super(context, new BearModel(context.bakeLayer(BearModel.LAYER_LOCATION)), new BearBabyModel(context.bakeLayer(BearBabyModel.LAYER_LOCATION)), 0.9F);
    }
}
