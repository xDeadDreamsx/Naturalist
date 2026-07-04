package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.KomodoDragon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class KomodoDragonModel extends GeoModel<KomodoDragon> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(KomodoDragon komodoDragon) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/komodo_dragon.png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(KomodoDragon komodoDragon) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/komodo_dragon.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(KomodoDragon komodoDragon) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/komodo_dragon.animation.json");
    }

    @Override
    public void setCustomAnimations(KomodoDragon entity, long instanceId, AnimationState<KomodoDragon> animationState) {
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
