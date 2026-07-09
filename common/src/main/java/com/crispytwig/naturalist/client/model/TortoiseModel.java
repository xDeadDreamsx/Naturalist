package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Tortoise;
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
public class TortoiseModel extends GeoModel<Tortoise> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(Tortoise tortoise) {
        if (tortoise.isBaby()) {
            return tortoise.getVariantBabyModel(Naturalist.location("geo/entity/tortoise_baby.geo.json"));
        }
        return tortoise.getVariantModel(Naturalist.location("geo/entity/tortoise.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(@NotNull Tortoise tortoise) {
        return tortoise.isBaby() ? tortoise.getVariantBabyTexture() : tortoise.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(Tortoise tortoise) {
        if (tortoise.isBaby()) {
            return tortoise.getVariantBabyAnimation(Naturalist.location("animations/tortoise_baby.animation.json"));
        }
        return tortoise.getVariantAnimation(Naturalist.location("animations/tortoise.animation.json"));
    }

    @Override
    public void setCustomAnimations(@NotNull Tortoise entity, long instanceId, AnimationState<Tortoise> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);

        if (head != null) {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }

        GeoBone asleep = this.getBone("asleep").orElse(null);
        if (asleep != null) asleep.setHidden(true);
    }
}
