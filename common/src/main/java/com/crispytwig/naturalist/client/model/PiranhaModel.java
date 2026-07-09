package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Piranha;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class PiranhaModel extends GeoModel<Piranha> {
    @Override
    public ResourceLocation getTextureResource(Piranha piranha) {
        return piranha.getVariantTexture();
    }

    @Override
    public ResourceLocation getModelResource(Piranha piranha) {
        return piranha.getVariantModel(Naturalist.location("geo/entity/piranha.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Piranha piranha) {
        return piranha.getVariantAnimation(Naturalist.location("animations/piranha.animation.json"));
    }

    @Override
    public void setCustomAnimations(Piranha piranha, long instanceId, @Nullable AnimationState<Piranha> animationState) {
        super.setCustomAnimations(piranha, instanceId, animationState);

        if (animationState == null) return;

        this.getBone("root").ifPresent(root -> {
            root.setRotX(root.getRotX() - piranha.getXBodyRot(animationState.getPartialTick()) * Mth.DEG_TO_RAD);
            root.resetStateChanges();
        });
    }
}
