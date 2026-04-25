package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Lizard;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class LizardModel extends GeoModel<Lizard> {
    public static final ResourceLocation[] TEXTURE_LOCATIONS = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/green.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/brown.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/beardie.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lizard/leopard_gecko.png")
    };

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Lizard lizard) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/lizard.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(@NotNull Lizard lizard) {
        return TEXTURE_LOCATIONS[Math.min(lizard.getVariant(), TEXTURE_LOCATIONS.length - 1)];
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(Lizard lizard) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/lizard.animation.json");
    }

    @Override
    public void setCustomAnimations(@NotNull Lizard entity, long instanceId, AnimationState<Lizard> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("neck").ifPresent(head -> {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        });

        this.getBone("tail").ifPresent(tail -> tail.setHidden(!entity.hasTail()));
    }
}
