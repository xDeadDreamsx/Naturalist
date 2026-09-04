package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.AntModel;
import com.crispytwig.naturalist.server.entity.mob.Ant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AntRenderer extends NaturalistSingleMobRenderer<Ant> {
    public AntRenderer(EntityRendererProvider.Context context) {
        super(context, new AntModel(context.bakeLayer(AntModel.LAYER_LOCATION)), 0.25F);
    }
}
