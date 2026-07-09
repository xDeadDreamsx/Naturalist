package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Clam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class ClamModel extends GeoModel<Clam> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Clam clam) {
        return clam.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Clam clam) {
        return clam.getVariantModel(Naturalist.location("geo/entity/clam.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Clam clam) {
        return clam.getVariantAnimation(Naturalist.location("animations/clam.animation.json"));
    }
}
