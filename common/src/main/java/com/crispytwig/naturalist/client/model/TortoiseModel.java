package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Tortoise;
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
public class TortoiseModel extends GeoModel<Tortoise> {
    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getModelResource(Tortoise tortoise) {
        if (tortoise.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/tortoise_baby.geo.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/tortoise.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(@NotNull Tortoise tortoise) {
        if (tortoise.isBaby()) {
            return switch (tortoise.getVariant()) {
                case 1 -> ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/tortoise/green_baby.png");
                case 2 -> ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/tortoise/black_baby.png");
                default -> ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/tortoise/brown_baby.png");
            };
        }
        return switch (tortoise.getVariant()) {
            case 1 -> ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/tortoise/green.png");
            case 2 -> ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/tortoise/black.png");
            default -> ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/tortoise/brown.png");
        };
    }

    @Override
    public ResourceLocation getAnimationResource(Tortoise tortoise) {
        if (tortoise.isBaby()) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/tortoise_baby.animation.json");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/tortoise.animation.json");
    }

    @Override
    public void setCustomAnimations(@NotNull Tortoise entity, long instanceId, AnimationState<Tortoise> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        GeoBone head = this.getBone("neck").orElse(null);

        if (head != null) {
            head.setRotX(head.getRotX() + extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            head.setRotY(head.getRotY() + extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            head.resetStateChanges();
        }

        GeoBone asleep = this.getBone("asleep").orElse(null);
        if (asleep != null) asleep.setHidden(true);
    }
}
