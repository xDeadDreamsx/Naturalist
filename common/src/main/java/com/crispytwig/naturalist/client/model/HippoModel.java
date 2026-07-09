package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Hippo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class HippoModel extends GeoModel<Hippo> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Hippo hippo) {
        if (hippo.isBaby()) {
            return hippo.getVariantBabyModel(Naturalist.location("geo/entity/hippo_baby.geo.json"));
        }
        return hippo.getVariantModel(Naturalist.location("geo/entity/hippo.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(Hippo hippo) {
        return hippo.isBaby() ? hippo.getVariantBabyTexture() : hippo.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Hippo hippo) {
        if (hippo.isBaby()) {
            return hippo.getVariantBabyAnimation(Naturalist.location("animations/hippo_baby.animation.json"));
        }
        return hippo.getVariantAnimation(Naturalist.location("animations/hippo.animation.json"));
    }

    @Override
    public void setCustomAnimations(Hippo entity, long instanceId, @Nullable AnimationState<Hippo> animationState) {
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
