package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Firefly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class FireflyModel extends GeoModel<Firefly> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Firefly firefly) {
        if (firefly.isBaby()) {
            return firefly.getVariantBabyModel(Naturalist.location("geo/entity/firefly_baby.geo.json"));
        }
        return firefly.getVariantModel(Naturalist.location("geo/entity/firefly.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Firefly firefly) {
        return firefly.isBaby() ? firefly.getVariantBabyTexture() : firefly.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Firefly firefly) {
        if (firefly.isBaby()) {
            return firefly.getVariantBabyAnimation(Naturalist.location("animations/firefly_baby.animation.json"));
        }
        return firefly.getVariantAnimation(Naturalist.location("animations/firefly.animation.json"));
    }
}
