package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.GiantIsopod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class GiantIsopodModel extends GeoModel<GiantIsopod> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(GiantIsopod isopod) {
        return isopod.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(GiantIsopod isopod) {
        return isopod.getVariantModel(Naturalist.location("geo/entity/giant_isopod.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(GiantIsopod isopod) {
        return isopod.getVariantAnimation(Naturalist.location("animations/giant_isopod.animation.json"));
    }
}
