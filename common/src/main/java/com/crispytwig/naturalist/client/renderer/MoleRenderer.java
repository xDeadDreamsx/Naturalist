package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.MoleModel;
import com.crispytwig.naturalist.server.entity.mob.Mole;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class MoleRenderer extends NaturalistSingleMobRenderer<Mole> {
    public MoleRenderer(EntityRendererProvider.Context context) {
        super(context, new MoleModel(context.bakeLayer(MoleModel.LAYER_LOCATION)), 0.4F);
    }
}
