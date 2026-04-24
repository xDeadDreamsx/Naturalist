package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Elephant;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class ElephantModel extends GeoModel<Elephant> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Elephant elephant) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/elephant.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Elephant elephant) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID,  "textures/entity/elephant.png");
    }

    @Override
    public @NotNull ResourceLocation getAnimationResource(Elephant elephant) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/elephant.animation.json");
    }

    @Override
    public void setCustomAnimations(Elephant entity, long instanceId, AnimationState<Elephant> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("head").orElse(null);
        GeoBone bigTusks = this.getBone("tusks").orElse(null);
        GeoBone smallTusks = this.getBone("baby_tusks").orElse(null);
        GeoBone babyTrunk = this.getBone("trunk4").orElse(null);
        GeoBone leftEar = this.getBone("left_ear").orElse(null);
        GeoBone rightEar = this.getBone("right_ear").orElse(null);

        if (head != null) {
            if (entity.isBaby()) {
                head.setScaleX(1.3F); head.setScaleY(1.3F); head.setScaleZ(1.3F);
            } else {
                head.setScaleX(1.0F); head.setScaleY(1.0F); head.setScaleZ(1.0F);
            }
            head.setRotX(extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
        }

        if (leftEar != null) {
            if (entity.isBaby()) {
                leftEar.setScaleX(1.2F); leftEar.setScaleY(1.2F); leftEar.setScaleZ(1.2F);
            } else {
                leftEar.setScaleX(1.0F); leftEar.setScaleY(1.0F); leftEar.setScaleZ(1.0F);
            }
        }

        if (rightEar != null) {
            if (entity.isBaby()) {
                rightEar.setScaleX(1.2F); rightEar.setScaleY(1.2F); rightEar.setScaleZ(1.2F);
            } else {
                rightEar.setScaleX(1.0F); rightEar.setScaleY(1.0F); rightEar.setScaleZ(1.0F);
            }
        }

        if (smallTusks != null) smallTusks.setHidden(!entity.isBaby());
        if (bigTusks != null) bigTusks.setHidden(entity.isBaby());
        if (babyTrunk != null) babyTrunk.setHidden(entity.isBaby());
    }
}
