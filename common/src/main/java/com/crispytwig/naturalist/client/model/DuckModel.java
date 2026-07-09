package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Duck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class DuckModel extends GeoModel<Duck> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Duck animal) {
        if (animal.isBaby()) {
            return animal.getVariantBabyModel(Naturalist.location("geo/entity/duck_baby.geo.json"));
        }
        return animal.getVariantModel(Naturalist.location("geo/entity/duck.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Duck animal) {
        if (animal.hasNonDefaultVariant()) {
            return animal.isBaby() ? animal.getVariantBabyTexture() : animal.getVariantTexture();
        }
        if (animal.isBaby()) {
            return Naturalist.location("textures/entity/duck/duck_baby.png");
        }
        if (animal.getName().getString().equalsIgnoreCase("Queso")) {
            return Naturalist.location("textures/entity/duck/queso.png");
        }
        return Naturalist.location("textures/entity/duck/duck.png");
    }

    @SuppressWarnings("unused")
    @Override
    public @NotNull ResourceLocation getAnimationResource(Duck animal) {
        if (animal.isBaby()) {
            return animal.getVariantBabyAnimation(Naturalist.location("animations/duck_baby.animation.json"));
        }
        return animal.getVariantAnimation(Naturalist.location("animations/duck.animation.json"));
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
