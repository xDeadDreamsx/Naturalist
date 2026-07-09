package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Crab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class CrabModel extends GeoModel<Crab> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Crab crab) {
        return crab.isBaby() ? crab.getVariantBabyTexture() : crab.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Crab crab) {
        if (crab.isBaby()) {
            return crab.getVariantBabyModel(Naturalist.location("geo/entity/crab_baby.geo.json"));
        }
        return crab.getVariantModel(Naturalist.location("geo/entity/crab.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Crab crab) {
        if (crab.isBaby()) {
            return crab.getVariantBabyAnimation(Naturalist.location("animations/crab_baby.animation.json"));
        }
        return crab.getVariantAnimation(Naturalist.location("animations/crab.animation.json"));
    }
}
