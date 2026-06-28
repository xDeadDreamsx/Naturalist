package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Crab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class CrabModel extends GeoModel<Crab> {
    private static final String[] TEXTURES = {"blue_crab", "brown_crab", "orange_crab", "red_crab", "yellow_crab"};

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Crab crab) {
        int variant = Math.floorMod(crab.getVariant(), TEXTURES.length);
        String name = TEXTURES[variant] + (crab.isBaby() ? "_baby" : "");
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/crab/" + name + ".png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Crab crab) {
        String geo = crab.isBaby() ? "crab_baby" : "crab";
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/" + geo + ".geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Crab crab) {
        String anim = crab.isBaby() ? "crab_baby" : "crab";
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/" + anim + ".animation.json");
    }
}
