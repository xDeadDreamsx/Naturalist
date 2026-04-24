package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Bass;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class BassModel extends GeoModel<Bass> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Bass bass) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/bass.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Bass bass) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bass.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Bass bass) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/bass.animation.json");
    }
}
