package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Bass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class BassModel extends GeoModel<Bass> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Bass bass) {
        return bass.getVariantModel(Naturalist.location("geo/entity/bass.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Bass bass) {
        return bass.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Bass bass) {
        return bass.getVariantAnimation(Naturalist.location("animations/bass.animation.json"));
    }
}
