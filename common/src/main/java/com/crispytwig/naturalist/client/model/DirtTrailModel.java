package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.misc.DirtTrail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class DirtTrailModel extends GeoModel<DirtTrail> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(DirtTrail trail) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/mole.png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(DirtTrail trail) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/dirt_trail.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(DirtTrail trail) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/dirt_trail.animation.json");
    }

    @Override
    public void setCustomAnimations(DirtTrail entity, long instanceId, AnimationState<DirtTrail> animationState) {
        super.setCustomAnimations(entity, instanceId, animationState);

        int bits = entity.getUUID().hashCode();
        this.getBone("extra1").ifPresent(bone -> bone.setHidden((bits & 1) == 0));
        this.getBone("extra2").ifPresent(bone -> bone.setHidden((bits & 2) == 0));
        this.getBone("extra3").ifPresent(bone -> bone.setHidden((bits & 4) == 0));
    }
}
