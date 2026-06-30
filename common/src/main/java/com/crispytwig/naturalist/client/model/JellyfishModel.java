package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Jellyfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class JellyfishModel extends GeoModel<Jellyfish> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Jellyfish jellyfish) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/jellyfish/" + jellyfish.getVariantName() + ".png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Jellyfish jellyfish) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/jellyfish.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Jellyfish jellyfish) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/jellyfish.animation.json");
    }

    @Override
    public void setCustomAnimations(Jellyfish jellyfish, long instanceId, @Nullable AnimationState<Jellyfish> animationState) {
        super.setCustomAnimations(jellyfish, instanceId, animationState);

        if (animationState == null) return;

        this.getBone("body").ifPresent(body -> {
            body.setRotX(body.getRotX() + jellyfish.getXBodyRot(animationState.getPartialTick()) * Mth.DEG_TO_RAD);
            body.resetStateChanges();
        });
    }
}
