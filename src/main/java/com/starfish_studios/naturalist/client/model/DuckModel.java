package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Duck;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;

@OnlyIn(Dist.CLIENT)
public class DuckModel extends GeoModel<Duck> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Duck animal) {
        if (animal.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/duck_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/duck.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Duck animal) {
        if (animal.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/duck/duck_baby.png");
        }
        if (animal.getName().getString().equalsIgnoreCase("Queso")) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/duck/queso.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/duck/duck.png");
    }

    @SuppressWarnings("unused")
    @Override
    public @NotNull ResourceLocation getAnimationResource(Duck animal) {
        if (animal.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/duck_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/duck.animation.json");
    }

    @Override
    public void setCustomAnimations(Duck entity, long instanceId, AnimationState<Duck> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);

        if (head != null) {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }
    }

}
