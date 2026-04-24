package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Caterpillar;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
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
