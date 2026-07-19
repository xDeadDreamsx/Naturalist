package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.ElephantBabyModel;
import com.crispytwig.naturalist.client.model.ElephantModel;
import com.crispytwig.naturalist.client.renderer.layers.SeatedRiderLayer;
import com.crispytwig.naturalist.server.entity.mob.Elephant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ElephantRenderer extends NaturalistMobRenderer<Elephant> {
    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new ElephantModel(context.bakeLayer(ElephantModel.LAYER_LOCATION)), new ElephantBabyModel(context.bakeLayer(ElephantBabyModel.LAYER_LOCATION)), 1.5F);
        this.addLayer(new SeatedRiderLayer<>(this));
    }
}
