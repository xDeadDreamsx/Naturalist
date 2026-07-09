package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.LizardTail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class LizardTailModel extends GeoModel<LizardTail> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(LizardTail lizard) {
        return lizard.getVariantModel(Naturalist.location("geo/entity/lizard_tail.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(LizardTail lizard) {
        return lizard.getVariantTexture();
    }

    @SuppressWarnings("unused")
    @Override
    public ResourceLocation getAnimationResource(LizardTail lizard) {
        return lizard.getVariantAnimation(Naturalist.location("animations/lizard_tail.animation.json"));
    }
}
