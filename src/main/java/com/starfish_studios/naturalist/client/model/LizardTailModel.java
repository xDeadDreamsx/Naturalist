package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.LizardTail;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class LizardTailModel extends GeoModel<LizardTail> {
    public static final ResourceLocation[] TEXTURE_LOCATIONS = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/green_tail.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/brown_tail.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/beardie_tail.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/leopard_gecko_tail.png"),
    };

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(LizardTail lizard) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/lizard_tail.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(LizardTail lizard) {
        return TEXTURE_LOCATIONS[lizard.getVariant()];
    }

    @SuppressWarnings("unused")
    @Override
    public ResourceLocation getAnimationResource(LizardTail lizard) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/lizard_tail.animation.json");
    }
}
