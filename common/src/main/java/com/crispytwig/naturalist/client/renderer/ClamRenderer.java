package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.ClamModel;
import com.crispytwig.naturalist.server.entity.mob.Clam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import com.crispytwig.naturalist.client.renderer.layers.ClamItemLayer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ClamRenderer extends NaturalistSingleMobRenderer<Clam> {
    public ClamRenderer(EntityRendererProvider.Context context) {
        super(context, new ClamModel(context.bakeLayer(ClamModel.LAYER_LOCATION)), 0.0F);
        this.addLayer(new ClamItemLayer(this, context.getItemModelResolver(), context.getEntityRenderDispatcher()));
    }
}
