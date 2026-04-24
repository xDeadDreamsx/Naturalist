package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Dragonfly;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class DragonflyModel extends GeoModel<Dragonfly> {
    public static final ResourceLocation[] TEXTURE_LOCATIONS = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/dragonfly/blue_dragonfly.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/dragonfly/green_dragonfly.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/dragonfly/red_dragonfly.png")
    };

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Dragonfly dragonfly) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/dragonfly.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Dragonfly dragonfly) {
        return TEXTURE_LOCATIONS[dragonfly.getVariant()];
    }

    @Override
    public ResourceLocation getAnimationResource(Dragonfly dragonfly) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/dragonfly.animation.json");
    }
}
