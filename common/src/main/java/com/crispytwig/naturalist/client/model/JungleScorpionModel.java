package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.JungleScorpion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class JungleScorpionModel extends GeoModel<JungleScorpion> {
    @Override
    public ResourceLocation getModelResource(JungleScorpion scorpion) {
        return scorpion.getVariantModel(Naturalist.location("geo/entity/jungle_scorpion.geo.json"));
    }

    @Override
    public ResourceLocation getTextureResource(JungleScorpion scorpion) {
        return scorpion.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(JungleScorpion scorpion) {
        return scorpion.getVariantAnimation(Naturalist.location("animations/jungle_scorpion.animation.json"));
    }
}
