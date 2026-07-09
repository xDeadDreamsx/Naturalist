package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Vulture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class VultureModel extends GeoModel<Vulture> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Vulture vulture) {
        if (vulture.isBaby()) {
            return vulture.getVariantBabyModel(Naturalist.location("geo/entity/vulture_baby.geo.json"));
        }
        return vulture.getVariantModel(Naturalist.location("geo/entity/vulture.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Vulture vulture) {
        return vulture.isBaby() ? vulture.getVariantBabyTexture() : vulture.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Vulture vulture) {
        if (vulture.isBaby()) {
            return vulture.getVariantBabyAnimation(Naturalist.location("animations/vulture_baby.animation.json"));
        }
        return vulture.getVariantAnimation(Naturalist.location("animations/vulture.animation.json"));
    }

    @Override
    public void setCustomAnimations(Vulture entity, long instanceId, AnimationState<Vulture> animationState) {
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
