package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.GreatWhiteShark;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class GreatWhiteSharkModel extends GeoModel<GreatWhiteShark> {
    private static final float HEAD_ROLL_STABILIZE = 0.35F;
    private static final float FLUKE_ROLL_FOLLOW = 0.4F;

    @Override
    public ResourceLocation getModelResource(GreatWhiteShark shark) {
        return shark.getVariantModel(Naturalist.location("geo/entity/great_white_shark.geo.json"));
    }

    @Override
    public ResourceLocation getTextureResource(GreatWhiteShark shark) {
        return shark.getVariantTexture();
    }

    @Override
    public ResourceLocation getAnimationResource(GreatWhiteShark shark) {
        return shark.getVariantAnimation(Naturalist.location("animations/great_white_shark.animation.json"));
    }

    @Override
    public void setCustomAnimations(GreatWhiteShark shark, long instanceId, @Nullable AnimationState<GreatWhiteShark> animationState) {
        super.setCustomAnimations(shark, instanceId, animationState);

        if (animationState == null) return;

        float partialTick = animationState.getPartialTick();
        float bodyPitch = shark.getXBodyRot(partialTick);
        float roll = shark.getZBodyRot(partialTick) * Mth.DEG_TO_RAD;

        this.rotateBone("root", -bodyPitch * Mth.DEG_TO_RAD, 0.0F, roll);
        this.bend("skullRot", shark, 0, partialTick);
        this.bend("tail", shark, 1, partialTick);
        this.bend("fluke", shark, 2, partialTick);
        this.rotateBone("skullRot", 0.0F, 0.0F, -roll * HEAD_ROLL_STABILIZE);
        this.rotateBone("fluke", 0.0F, 0.0F, roll * FLUKE_ROLL_FOLLOW);
    }

    private void bend(String boneName, GreatWhiteShark shark, int segment, float partialTick) {
        this.rotateBone(boneName,
                -shark.getSegPitchOffset(segment, partialTick) * Mth.DEG_TO_RAD,
                -shark.getSegYawOffset(segment, partialTick) * Mth.DEG_TO_RAD,
                0.0F);
    }

    private void rotateBone(String boneName, float dx, float dy, float dz) {
        this.getBone(boneName).ifPresent(bone -> {
            bone.setRotX(bone.getRotX() + dx);
            bone.setRotY(bone.getRotY() + dy);
            bone.setRotZ(bone.getRotZ() + dz);
            bone.resetStateChanges();
        });
    }
}
