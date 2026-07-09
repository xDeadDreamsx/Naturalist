package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.BlackBear;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class BlackBearModel extends GeoModel<BlackBear> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(BlackBear bear) {
        if (bear.isBaby()) {
            return bear.getVariantBabyModel(Naturalist.location("geo/entity/black_bear_baby.geo.json"));
        }
        return bear.getVariantModel(Naturalist.location("geo/entity/black_bear.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(@NotNull BlackBear bear) {
        return bear.isBaby() ? bear.getVariantBabyTexture() : bear.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(BlackBear bear) {
        if (bear.isBaby()) {
            return bear.getVariantBabyAnimation(Naturalist.location("animations/black_bear_baby.animation.json"));
        }
        return bear.getVariantAnimation(Naturalist.location("animations/black_bear.animation.json"));
    }

    @Override
    public void setCustomAnimations(BlackBear entity, long instanceId, AnimationState<BlackBear> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone(entity.isBaby() ? "skull" : "skullRot").ifPresent(head -> {
            if (!entity.isSleeping() && !entity.isEating() && !entity.isSitting()) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        });

        boolean sleeping = entity.isSleeping();
        boolean angry = (entity.isAngry() || entity.isAggressive()) && !entity.isBaby();
        boolean holdingBerries = entity.getMainHandItem().is(Items.SWEET_BERRIES);
        boolean holdingHoney = entity.getMainHandItem().is(Items.HONEYCOMB);
        boolean holdingVariant = holdingBerries || holdingHoney;

        this.getBone("sleep").ifPresent(bone -> bone.setHidden(!sleeping));
        this.getBone("head_angry").ifPresent(bone -> bone.setHidden(sleeping || !angry));
        this.getBone("snout").ifPresent(bone -> {
            bone.setHidden(holdingVariant || angry);
            bone.setChildrenHidden(false);
        });
        this.getBone("snout_angry").ifPresent(bone -> bone.setHidden(holdingVariant || sleeping || !angry));
        this.getBone("right_arm").ifPresent(bone -> {
            bone.setHidden(holdingVariant);
            bone.setChildrenHidden(false);
        });
        this.getBone("left_arm").ifPresent(bone -> {
            bone.setHidden(holdingVariant);
            bone.setChildrenHidden(false);
        });
        this.getBone("snout_berries").ifPresent(bone -> bone.setHidden(!holdingBerries));
        this.getBone("left_arm_berries").ifPresent(bone -> bone.setHidden(!holdingBerries));
        this.getBone("right_arm_berries").ifPresent(bone -> bone.setHidden(!holdingBerries));
        this.getBone("snout_honey").ifPresent(bone -> bone.setHidden(!holdingHoney));
        this.getBone("left_arm_honey").ifPresent(bone -> bone.setHidden(!holdingHoney));
        this.getBone("right_arm_honey").ifPresent(bone -> bone.setHidden(!holdingHoney));
    }
}
