package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Rat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class RatModel extends GeoModel<Rat> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Rat rat) {
        return rat.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Rat rat) {
        return rat.getVariantModel(Naturalist.location("geo/entity/rat.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Rat rat) {
        return rat.getVariantAnimation(Naturalist.location("animations/rat.animation.json"));
    }

    @Override
    public void setCustomAnimations(Rat entity, long instanceId, AnimationState<Rat> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        boolean sleeping = entity.isSleeping();
        this.getBone("awake").ifPresent(bone -> bone.setHidden(sleeping));
        this.getBone("sleep").ifPresent(bone -> bone.setHidden(!sleeping));

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("skull").ifPresent(skull -> {
            if (!sleeping && !entity.isInSittingPose()) {
                skull.setRotX(skull.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                skull.setRotY(skull.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                skull.resetStateChanges();
            }
        });
    }
}
