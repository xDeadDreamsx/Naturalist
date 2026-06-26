package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Caterpillar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class CaterpillarModel extends GeoModel<Caterpillar> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Caterpillar object) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/caterpillar.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Caterpillar object) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/caterpillar.png");
    }

    @SuppressWarnings("unused")
    @Override
    public @NotNull ResourceLocation getAnimationResource(Caterpillar animatable) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/caterpillar.animation.json");
    }
}
