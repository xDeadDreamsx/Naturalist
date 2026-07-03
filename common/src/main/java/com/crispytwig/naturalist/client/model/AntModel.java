package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Ant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class AntModel extends GeoModel<Ant> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Ant ant) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/ant.png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Ant ant) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/ant.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Ant ant) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/ant.animation.json");
    }
}
