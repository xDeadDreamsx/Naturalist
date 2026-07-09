package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Blobfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class BlobfishModel extends GeoModel<Blobfish> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Blobfish blobfish) {
        if (blobfish.hasNonDefaultVariant()) {
            return blobfish.getVariantTexture();
        }
        String form = blobfish.isGray() ? "gray" : "pink";
        return Naturalist.location("textures/entity/blobfish/" + form + ".png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Blobfish blobfish) {
        if (blobfish.hasNonDefaultVariant()) {
            return blobfish.getVariantModel(Naturalist.location("geo/entity/blobfish_pink.geo.json"));
        }
        String form = blobfish.isGray() ? "gray" : "pink";
        return Naturalist.location("geo/entity/blobfish_" + form + ".geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Blobfish blobfish) {
        return blobfish.getVariantAnimation(Naturalist.location("animations/blobfish.animation.json"));
    }

    @Override
    public void setCustomAnimations(Blobfish blobfish, long instanceId, @Nullable AnimationState<Blobfish> animationState) {
        super.setCustomAnimations(blobfish, instanceId, animationState);

        if (animationState == null) return;

        this.getBone("root").ifPresent(root -> {
            root.setRotX(root.getRotX() - blobfish.getXBodyRot(animationState.getPartialTick()) * Mth.DEG_TO_RAD);
            if (blobfish.isConverting()) {
                float shiver = Mth.cos((blobfish.tickCount + animationState.getPartialTick()) * 3.0F) * 0.1F;
                root.setRotY(root.getRotY() + shiver);
            }
            root.resetStateChanges();
        });
    }
}
