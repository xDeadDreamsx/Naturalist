package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Alligator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class AlligatorModel extends GeoModel<Alligator> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Alligator alligator) {
        if (alligator.isBaby()) {
            return alligator.getVariantBabyModel(Naturalist.location("geo/entity/alligator_baby.geo.json"));
        }
        return alligator.getVariantModel(Naturalist.location("geo/entity/alligator.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Alligator alligator) {
        return alligator.isBaby() ? alligator.getVariantBabyTexture() : alligator.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Alligator alligator) {
        if (alligator.isBaby()) {
            return alligator.getVariantBabyAnimation(Naturalist.location("animations/alligator_baby.animation.json"));
        }
        return alligator.getVariantAnimation(Naturalist.location("animations/alligator.animation.json"));
    }

    @Override
    public void setCustomAnimations(Alligator entity, long instanceId, @Nullable AnimationState<Alligator> animationState) {
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
