package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Catfish;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
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
