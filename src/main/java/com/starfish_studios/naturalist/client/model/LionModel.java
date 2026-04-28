package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Lion;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class LionModel extends GeoModel<Lion> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(Lion entity) {
        if (entity.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/lion_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/lion.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Lion entity) {
        if (entity.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lion/lion_baby.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/lion/lion.png");
    }

    @SuppressWarnings("unused")
    @Override
    public ResourceLocation getAnimationResource(Lion entity) {
        if (entity.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/lion_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/lion.animation.json");
    }

    @Override
    public void setCustomAnimations(Lion entity, long instanceId, @Nullable AnimationState<Lion> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);
        GeoBone mane = this.getBone("mane").orElse(null);

        if (head != null) {
            if (!entity.isSleeping()) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        }

        if (mane != null) {
            mane.setHidden(!entity.hasMane());
        }

        boolean angry = entity.isAggressive();
        boolean sleeping = entity.isSleeping() && !angry;
        GeoBone awake = this.getBone("awake").orElse(null);
        GeoBone asleep = this.getBone("asleep").orElse(null);
        if (awake != null) awake.setHidden(sleeping || angry);
        if (asleep != null) asleep.setHidden(!sleeping);
        this.getBone("angry").ifPresent(bone -> bone.setHidden(!angry));
    }
}
