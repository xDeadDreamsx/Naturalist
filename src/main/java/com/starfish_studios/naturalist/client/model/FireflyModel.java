package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Firefly;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class FireflyModel extends GeoModel<Firefly> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Firefly firefly) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/firefly.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Firefly firefly) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/firefly.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Firefly firefly) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/firefly.animation.json");
    }
}
