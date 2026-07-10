package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Bird;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class BirdModel extends GeoModel<Bird> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Bird bird) {
        return bird.isBaby() ? bird.getVariantBabyTexture() : bird.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Bird bird) {
        if (bird.isBaby()) {
            return bird.getVariantBabyModel(Naturalist.location("geo/entity/bird_baby.geo.json"));
        }
        return bird.getVariantModel(Naturalist.location("geo/entity/bird.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Bird bird) {
        if (bird.isBaby()) {
            return bird.getVariantBabyAnimation(Naturalist.location("animations/bird_baby.animation.json"));
        }
        return bird.getVariantAnimation(Naturalist.location("animations/bird.animation.json"));
    }

    @Override
    public void setCustomAnimations(Bird entity, long instanceId, @Nullable AnimationState<Bird> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("head").ifPresent(head -> {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        });

        this.getBone("root").ifPresent(root -> {
            if (!entity.onGround()) {
                float targetRotX = (float) (extraDataOfType.headPitch() * (Mth.PI / 360F) + entity.getDeltaMovement().y * 3.0F);
                root.setRotX(root.getRotX() + 0.1F * (targetRotX - root.getRotX()));
                root.setRotZ(extraDataOfType.netHeadYaw() * -(Mth.PI / 600F));
            }
            root.resetStateChanges();
        });
    }
}
