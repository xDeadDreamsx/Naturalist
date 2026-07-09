package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Boar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class BoarModel extends GeoModel<Boar> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Boar boar) {
        if (boar.isBaby()) {
            return boar.getVariantBabyModel(Naturalist.location("geo/entity/boar_baby.geo.json"));
        }
        return boar.getVariantModel(Naturalist.location("geo/entity/boar.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Boar boar) {
        return boar.isBaby() ? boar.getVariantBabyTexture() : boar.getVariantTexture();
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(Boar boar) {
        if (boar.isBaby()) {
            return boar.getVariantBabyAnimation(Naturalist.location("animations/boar_baby.animation.json"));
        }
        return boar.getVariantAnimation(Naturalist.location("animations/boar.animation.json"));
    }

    @Override
    public void setCustomAnimations(Boar entity, long instanceId, AnimationState<Boar> animationState) {
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
