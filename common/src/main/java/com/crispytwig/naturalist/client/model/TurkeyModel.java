package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Turkey;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class TurkeyModel extends GeoModel<Turkey> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Turkey turkey) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/turkey.png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Turkey turkey) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/turkey.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Turkey turkey) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/turkey.animation.json");
    }

    @Override
    public void setCustomAnimations(Turkey entity, long instanceId, AnimationState<Turkey> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("skull").ifPresent(head -> {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        });
    }
}
