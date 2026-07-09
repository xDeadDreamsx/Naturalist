package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Tiger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class TigerModel extends GeoModel<Tiger> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Tiger tiger) {
        return tiger.isBaby() ? tiger.getVariantBabyTexture() : tiger.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Tiger tiger) {
        if (tiger.isBaby()) {
            return tiger.getVariantBabyModel(Naturalist.location("geo/entity/tiger_baby.geo.json"));
        }
        return tiger.getVariantModel(Naturalist.location("geo/entity/tiger.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Tiger tiger) {
        if (tiger.isBaby()) {
            return tiger.getVariantBabyAnimation(Naturalist.location("animations/tiger_baby.animation.json"));
        }
        return tiger.getVariantAnimation(Naturalist.location("animations/tiger.animation.json"));
    }

    @Override
    public void setCustomAnimations(Tiger entity, long instanceId, AnimationState<Tiger> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        boolean sleeping = entity.isSleeping() || entity.isInSittingPose();
        this.getBone("sleep").ifPresent(bone -> bone.setHidden(!sleeping));
        this.getBone("awake").ifPresent(bone -> bone.setHidden(sleeping));
        this.getBone("asleep").ifPresent(bone -> bone.setHidden(!sleeping));

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone(entity.isBaby() ? "skull" : "skullRot").ifPresent(head -> {
            if (!sleeping) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        });
    }
}
