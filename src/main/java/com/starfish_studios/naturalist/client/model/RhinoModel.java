package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Rhino;
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
public class RhinoModel extends GeoModel<Rhino> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Rhino rhino) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/rhino.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Rhino rhino) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/rhino.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Rhino rhino) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/rhino.animation.json");
    }

    @Override
    public void setCustomAnimations(@NotNull Rhino entity, long instanceId, AnimationState<Rhino> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);
        GeoBone bigHorn = this.getBone("big_horn").orElse(null);
        GeoBone smallHorn = this.getBone("small_horn").orElse(null);
        GeoBone babyHorn = this.getBone("baby_horn").orElse(null);
        GeoBone leftEar = this.getBone("left_ear").orElse(null);
        GeoBone rightEar = this.getBone("right_ear").orElse(null);

        if (head != null) {
            if (entity.isBaby()) {
                head.setScaleX(1.4F);
                head.setScaleY(1.4F);
                head.setScaleZ(1.4F);
            } else {
                head.setScaleX(1.0F);
                head.setScaleY(1.0F);
                head.setScaleZ(1.0F);
            }

            if (!entity.isSprinting()) {
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            }
            head.resetStateChanges();
        }

        if (leftEar != null) {
            if (entity.isBaby()) {
                leftEar.setScaleX(1.1F);
                leftEar.setScaleY(1.1F);
                leftEar.setScaleZ(1.1F);
            } else {
                leftEar.setScaleX(1.0F);
                leftEar.setScaleY(1.0F);
                leftEar.setScaleZ(1.0F);
            }
            leftEar.resetStateChanges();
        }

        if (rightEar != null) {
            if (entity.isBaby()) {
                rightEar.setScaleX(1.1F);
                rightEar.setScaleY(1.1F);
                rightEar.setScaleZ(1.1F);
            } else {
                rightEar.setScaleX(1.0F);
                rightEar.setScaleY(1.0F);
                rightEar.setScaleZ(1.0F);
            }
            rightEar.resetStateChanges();
        }

        if (bigHorn != null) bigHorn.setHidden(entity.isBaby());
        if (smallHorn != null) smallHorn.setHidden(entity.isBaby());
        if (babyHorn != null) babyHorn.setHidden(!entity.isBaby());
    }
}
