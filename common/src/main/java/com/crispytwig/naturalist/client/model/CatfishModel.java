package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Catfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class CatfishModel extends GeoModel<Catfish> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Catfish catfish) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/catfish.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(Catfish catfish) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/catfish.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Catfish catfish) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/catfish.animation.json");
    }
}
