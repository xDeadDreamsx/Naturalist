package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.Hippo;
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
public class HippoModel extends GeoModel<Hippo> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Hippo hippo) {
        if (hippo.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/hippo_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/hippo.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(Hippo hippo) {
        if (hippo.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/hippo/hippo_baby.png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/hippo/hippo.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Hippo hippo) {
        if (hippo.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/hippo_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/hippo.animation.json");
    }

    @Override
    public void setCustomAnimations(Hippo entity, long instanceId, @Nullable AnimationState<Hippo> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);

        if (head != null) {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }
    }
}
