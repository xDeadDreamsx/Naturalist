package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Bear;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class BearModel extends GeoModel<Bear> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(Bear bear) {
        if (bear.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/bear_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/bear.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(@NotNull Bear bear) {
        if (bear.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bear/bear_baby.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/bear/bear.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Bear bear) {
        if (bear.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/bear_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/bear.animation.json");
    }

    @Override
    public void setCustomAnimations(Bear entity, long instanceId, AnimationState<Bear> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("neck").ifPresent(head -> {
            if (!entity.isSleeping() && !entity.isEating() && !entity.isSitting()) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        });

        boolean sheared = entity.isSheared();
        this.getBone("normalBody").ifPresent(bone -> bone.setHidden(sheared));
        this.getBone("shearedBody").ifPresent(bone -> bone.setHidden(!sheared));

        boolean angry = entity.isAngry() || entity.isAggressive();
        boolean sleeping = entity.isSleeping() && !angry;
        this.getBone("awake").ifPresent(bone -> bone.setHidden(sleeping || angry));
        this.getBone("asleep").ifPresent(bone -> bone.setHidden(!sleeping));
        this.getBone("angry").ifPresent(bone -> bone.setHidden(!angry));
        this.getBone("angrySnout").ifPresent(bone -> bone.setHidden(!angry));

        boolean eatingBerries = entity.isEating() && entity.getMainHandItem().is(Items.SWEET_BERRIES);
        this.getBone("berryArm").ifPresent(bone -> bone.setHidden(!eatingBerries));

        boolean eatingHoney = entity.isEating() && entity.getMainHandItem().is(Items.HONEYCOMB);
        this.getBone("honeyArm").ifPresent(bone -> bone.setHidden(!eatingHoney));
    }
}
