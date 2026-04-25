package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Deer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@OnlyIn(Dist.CLIENT)
public class DeerModel extends GeoModel<Deer> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Deer deer) {
        if (deer.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/deer_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/deer.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Deer deer) {
        if (deer.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/deer/deer_baby.png");
        }

        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/deer/deer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Deer deer) {
        if (deer.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/deer_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/deer.animation.json");
    }

    @Override
    public void setCustomAnimations(@NotNull Deer entity, long instanceId, AnimationState<Deer> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        this.getBone("neck").ifPresent(head -> {
            if (!entity.isEating()) {
                head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
                head.resetStateChanges();
            }
        });

        this.getBone("sleep").ifPresent(bone -> bone.setHidden(true));
        this.getBone("saddle").ifPresent(bone -> bone.setHidden(true));
    }

}
