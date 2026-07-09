package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Anglerfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class AnglerfishModel extends GeoModel<Anglerfish> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Anglerfish anglerfish) {
        return anglerfish.getVariantTexture();
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Anglerfish anglerfish) {
        return anglerfish.getVariantModel(Naturalist.location("geo/entity/anglerfish.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Anglerfish anglerfish) {
        return anglerfish.getVariantAnimation(Naturalist.location("animations/anglerfish.animation.json"));
    }

    @Override
    public void setCustomAnimations(Anglerfish anglerfish, long instanceId, @Nullable AnimationState<Anglerfish> animationState) {
        super.setCustomAnimations(anglerfish, instanceId, animationState);

        if (animationState == null) return;

        this.getBone("body").ifPresent(body -> {
            body.setRotX(body.getRotX() - anglerfish.getXBodyRot(animationState.getPartialTick()) * Mth.DEG_TO_RAD);
            body.resetStateChanges();
        });
    }
}
