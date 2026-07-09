package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Starfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class StarfishModel extends GeoModel<Starfish> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Starfish starfish) {
        return starfish.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Starfish starfish) {
        return starfish.getVariantModel(Naturalist.location("geo/entity/starfish.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Starfish starfish) {
        return starfish.getVariantAnimation(Naturalist.location("animations/starfish.animation.json"));
    }
}
