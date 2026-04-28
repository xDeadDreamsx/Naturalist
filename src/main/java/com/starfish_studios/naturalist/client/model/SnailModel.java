package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Snail;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class SnailModel extends GeoModel<Snail> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Snail snail) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/snail.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(@NotNull Snail snail) {
        if (snail.getSnailColor() != null) {
            int color = snail.getSnailColor().getId();
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snail/" + DyeColor.byId(color).getName() + ".png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snail/snail.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Snail snail) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/snail.animation.json");
    }

    @Override
    public void setCustomAnimations(Snail animatable, long instanceId, @Nullable AnimationState<Snail> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("eyes").ifPresent(eyes -> {
            if (animatable.isBaby()) {
                eyes.setScaleX(1.5F);
                eyes.setScaleY(1.5F);
                eyes.setScaleZ(1.5F);
            } else {
                eyes.setScaleX(1.0F);
                eyes.setScaleY(1.0F);
                eyes.setScaleZ(1.0F);
            }
            eyes.resetStateChanges();
        });

        if (!animatable.isNaturalistClimbing() || !animatable.canHide()) {
            this.getBone("left_eye").ifPresent(leftEye -> {
                leftEye.setRotX(leftEye.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                leftEye.setRotY(leftEye.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                leftEye.resetStateChanges();
            });
            this.getBone("right_eye").ifPresent(rightEye -> {
                rightEye.setRotX(rightEye.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                rightEye.setRotY(rightEye.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                rightEye.resetStateChanges();
            });
        }
    }
}
