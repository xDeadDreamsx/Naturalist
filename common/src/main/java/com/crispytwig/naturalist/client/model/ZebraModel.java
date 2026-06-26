package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Zebra;
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
public class ZebraModel extends GeoModel<Zebra> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(Zebra zebra) {
        if (zebra.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/zebra_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/zebra.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Zebra zebra) {
        if (zebra.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/zebra_baby.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/zebra.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Zebra zebra) {
        if (zebra.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/zebra_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/zebra.animation.json");
    }

    @Override
    public void setCustomAnimations(Zebra entity, long instanceId, AnimationState<Zebra> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone neck = this.getBone("neck").orElse(null);

        if (neck != null) {
            neck.setRotX(neck.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            neck.setRotY(neck.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            neck.resetStateChanges();
        }

        this.getBone("saddle").ifPresent(saddle -> saddle.setHidden(!entity.isSaddled()));

        boolean saddled = entity.isTamed() && entity.isSaddled();
        this.getBone("bridle").ifPresent(bridle -> bridle.setHidden(!saddled));
        this.getBone("reinsR").ifPresent(reinsR -> reinsR.setHidden(!saddled || !entity.isVehicle()));
        this.getBone("reinsL").ifPresent(reinsL -> reinsL.setHidden(!saddled || !entity.isVehicle()));

        this.getBone("chest").ifPresent(chest -> chest.setHidden(!entity.hasChest()));
    }
}
