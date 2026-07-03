package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Mole;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class MoleModel extends GeoModel<Mole> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Mole mole) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/mole.png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Mole mole) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/mole.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Mole mole) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/mole.animation.json");
    }

    @Override
    public void setCustomAnimations(Mole entity, long instanceId, AnimationState<Mole> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null || entity.isRolledUp()) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("skull").ifPresent(skull -> {
            skull.setRotX(skull.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            skull.setRotY(skull.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            skull.resetStateChanges();
        });
    }
}
