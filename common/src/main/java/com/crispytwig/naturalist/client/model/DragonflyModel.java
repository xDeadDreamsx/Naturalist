package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Dragonfly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class DragonflyModel extends GeoModel<Dragonfly> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Dragonfly dragonfly) {
        return dragonfly.getVariantModel(Naturalist.location("geo/entity/dragonfly.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Dragonfly dragonfly) {
        return dragonfly.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Dragonfly dragonfly) {
        return dragonfly.getVariantAnimation(Naturalist.location("animations/dragonfly.animation.json"));
    }
}
