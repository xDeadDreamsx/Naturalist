package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.KomodoDragonModel;
import com.crispytwig.naturalist.server.entity.mob.KomodoDragon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class KomodoDragonRenderer extends NaturalistSingleMobRenderer<KomodoDragon> {
    public KomodoDragonRenderer(EntityRendererProvider.Context context) {
        super(context, new KomodoDragonModel(context.bakeLayer(KomodoDragonModel.LAYER_LOCATION)), 0.65F);
    }
}
