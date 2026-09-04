package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.HippoBabyModel;
import com.crispytwig.naturalist.client.model.HippoModel;
import com.crispytwig.naturalist.server.entity.mob.Hippo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.BlockItem;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class HippoRenderer extends NaturalistMobRenderer<Hippo> {
    public HippoRenderer(EntityRendererProvider.Context context) {
        super(context, new HippoModel(context.bakeLayer(HippoModel.LAYER_LOCATION)), new HippoBabyModel(context.bakeLayer(HippoBabyModel.LAYER_LOCATION)), 1.1F);
    }
}
