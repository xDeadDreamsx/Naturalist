package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Bird;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class BirdModel extends GeoModel<Bird> {

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Bird bird) {
        if (bird.isBaby()) {
            if (bird.getType().equals(NaturalistEntityTypes.BLUEJAY.get())) {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bird_baby_blue_jay.png");
            } else if (bird.getType().equals(NaturalistEntityTypes.CANARY.get())) {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bird_baby_canary.png");
            } else if (bird.getType().equals(NaturalistEntityTypes.CARDINAL.get())) {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bird_baby_cardinal.png");
            } else if (bird.getType().equals(NaturalistEntityTypes.FINCH.get())) {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bird_baby_finch.png");
            } else if (bird.getType().equals(NaturalistEntityTypes.SPARROW.get())) {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bird_baby_sparrow.png");
            } else {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bird_baby_robin.png");
            }
        }

        if (bird.getType().equals(NaturalistEntityTypes.BLUEJAY.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/bluejay.png");
        } else if (bird.getType().equals(NaturalistEntityTypes.CANARY.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/canary.png");
        } else if (bird.getType().equals(NaturalistEntityTypes.CARDINAL.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/cardinal.png");
        } else if (bird.getType().equals(NaturalistEntityTypes.FINCH.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/finch.png");
        } else if (bird.getType().equals(NaturalistEntityTypes.SPARROW.get())) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/sparrow.png");
        } else {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bird/robin.png");
        }
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Bird bird) {
        if (bird.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/bird_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/bird.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Bird bird) {
        if (bird.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/bird_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/bird.animation.json");
    }

    @Override
    public void setCustomAnimations(Bird entity, long instanceId, @Nullable AnimationState<Bird> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("neck").ifPresent(head -> {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        });
    }
}
