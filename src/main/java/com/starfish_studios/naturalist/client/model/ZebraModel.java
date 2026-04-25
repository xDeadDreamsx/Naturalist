package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Zebra;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
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

        GeoBone saddle = this.getBone("saddle").orElse(null);
        if (saddle != null) saddle.setHidden(!entity.isSaddled());

        boolean saddled = entity.isTamed() && entity.isSaddled();
        GeoBone bridle = this.getBone("bridle").orElse(null);
        if (bridle != null) bridle.setHidden(!saddled);
        GeoBone reinsR = this.getBone("reinsR").orElse(null);
        if (reinsR != null) reinsR.setHidden(!saddled || !entity.isVehicle());
        GeoBone reinsL = this.getBone("reinsL").orElse(null);
        if (reinsL != null) reinsL.setHidden(!saddled || !entity.isVehicle());

        GeoBone chest = this.getBone("chest").orElse(null);
        if (chest != null) chest.setHidden(!entity.hasChest());
    }
}
