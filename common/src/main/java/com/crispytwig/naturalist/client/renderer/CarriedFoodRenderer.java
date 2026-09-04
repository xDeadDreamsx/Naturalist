package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.server.entity.misc.CarriedFoodEntity;
import com.crispytwig.naturalist.server.entity.mob.Ant;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CarriedFoodRenderer extends ItemEntityRenderer {
    public CarriedFoodRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }
}
