package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.DesertScorpion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class DesertScorpionModel extends GeoModel<DesertScorpion> {
    @Override
    public ResourceLocation getModelResource(DesertScorpion scorpion) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/desert_scorpion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DesertScorpion scorpion) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/scorpion/desert_scorpion.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DesertScorpion scorpion) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/desert_scorpion.animation.json");
    }
}
