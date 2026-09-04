package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BlackBearBabyModel;
import com.crispytwig.naturalist.client.model.BlackBearModel;
import com.crispytwig.naturalist.server.entity.mob.BlackBear;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BlackBearRenderer extends NaturalistMobRenderer<BlackBear> {
    public BlackBearRenderer(EntityRendererProvider.Context context) {
        super(context, new BlackBearModel(context.bakeLayer(BlackBearModel.LAYER_LOCATION)), new BlackBearBabyModel(context.bakeLayer(BlackBearBabyModel.LAYER_LOCATION)), 0.9F);
    }
}
