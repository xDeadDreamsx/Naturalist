package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Ray;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class RayModel extends GeoModel<Ray> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getTextureResource(Ray ray) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/ray/" + ray.getVariantName() + ".png");
    }

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Ray ray) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/ray.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(Ray ray) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "animations/ray.animation.json");
    }

    @Override
    public void setCustomAnimations(Ray ray, long instanceId, @Nullable AnimationState<Ray> animationState) {
        super.setCustomAnimations(ray, instanceId, animationState);

        if (animationState == null) return;

        float counterGain = 1.6F;
        float partialTick = animationState.getPartialTick();
        float body = ray.getXBodyRot(partialTick);
        float finCounter = (body - ray.getFinLag(partialTick)) * Mth.DEG_TO_RAD * counterGain;
        float tailCounter = (body - ray.getTailLag(partialTick)) * Mth.DEG_TO_RAD * counterGain;

        this.getBone("body").ifPresent(bone -> {
            bone.setRotX(bone.getRotX() - body * Mth.DEG_TO_RAD);
            bone.setRotZ(bone.getRotZ() + ray.getZBodyRot(partialTick) * Mth.DEG_TO_RAD);
            bone.resetStateChanges();
        });
        this.counterRotate("leftFin", finCounter);
        this.counterRotate("rightFin", finCounter);
        this.counterRotate("tail", tailCounter);
    }

    private void counterRotate(String bone, float radians) {
        this.getBone(bone).ifPresent(b -> {
            b.setRotX(b.getRotX() + radians);
            b.resetStateChanges();
        });
    }
}
