package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Butterfly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class ButterflyModel extends GeoModel<Butterfly> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(Butterfly butterfly) {
        return butterfly.getVariantModel(Naturalist.location("geo/entity/butterfly.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(Butterfly butterfly) {
        return butterfly.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Butterfly butterfly) {
        return butterfly.getVariantAnimation(Naturalist.location("animations/butterfly.animation.json"));
    }
}
