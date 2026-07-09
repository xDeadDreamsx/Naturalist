package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Ostrich;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class OstrichModel extends GeoModel<Ostrich> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Ostrich ostrich) {
        return ostrich.isBaby() ? ostrich.getVariantBabyTexture() : ostrich.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Ostrich ostrich) {
        if (ostrich.isBaby()) {
            return ostrich.getVariantBabyModel(Naturalist.location("geo/entity/ostrich_baby.geo.json"));
        }
        return ostrich.getVariantModel(Naturalist.location("geo/entity/ostrich.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Ostrich ostrich) {
        if (ostrich.isBaby()) {
            return ostrich.getVariantBabyAnimation(Naturalist.location("animations/ostrich_baby.animation.json"));
        }
        return ostrich.getVariantAnimation(Naturalist.location("animations/ostrich.animation.json"));
    }

    @Override
    public void setCustomAnimations(Ostrich entity, long instanceId, AnimationState<Ostrich> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        this.getBone("saddle").ifPresent(saddle -> saddle.setHidden(!entity.isSaddled()));

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("skull").ifPresent(head -> {
            if (entity.isVehicle() && entity.getControllingPassenger() instanceof Player player) {
                float yaw = Mth.clamp(Mth.wrapDegrees(player.yHeadRot - entity.yBodyRot), -60.0F, 60.0F);
                float pitch = Mth.clamp(player.getXRot(), -30.0F, 60.0F);
                head.setRotX(head.getRotX() - pitch * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() - yaw * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            } else if (entity.isBaby() || !entity.canHide()) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        });
    }
}
