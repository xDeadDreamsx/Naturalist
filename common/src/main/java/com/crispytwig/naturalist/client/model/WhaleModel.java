package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.mob.Whale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class WhaleModel extends GeoModel<Whale> {

    private static final float YAW_GAIN = -1.0F;
    private static final float FIN_COUNTER_GAIN = 1.6F;
    private static final float HEAD_ROLL_STABILIZE = 0.35F;
    private static final float FLUKE_ROLL_FOLLOW = 0.4F;
    private static final float HEAD_DROOP_ANGLE = (float) Math.toRadians(-45.0D);
    private static final float TAIL_DROOP_ANGLE = (float) Math.toRadians(90.0D);

    @Override
    public ResourceLocation getTextureResource(Whale whale) {
        return whale.isBaby() ? whale.getVariantBabyTexture() : whale.getVariantTexture();
    }

    @Override
    public ResourceLocation getModelResource(Whale whale) {
        if (whale.isBaby()) {
            return whale.getVariantBabyModel(Naturalist.location("geo/entity/whale_baby.geo.json"));
        }
        return whale.getVariantModel(Naturalist.location("geo/entity/whale.geo.json"));
    }

    @Override
    public ResourceLocation getAnimationResource(Whale whale) {
        if (whale.isBaby()) {
            return whale.getVariantBabyAnimation(Naturalist.location("animations/whale_baby.animation.json"));
        }
        return whale.getVariantAnimation(Naturalist.location("animations/whale.animation.json"));
    }

    @Override
    public void setCustomAnimations(Whale whale, long instanceId, @Nullable AnimationState<Whale> animationState) {
        super.setCustomAnimations(whale, instanceId, animationState);

        if (animationState == null) return;

        float partialTick = animationState.getPartialTick();
        float bodyPitch = whale.getXBodyRot(partialTick);
        float roll = whale.getZBodyRot(partialTick) * Mth.DEG_TO_RAD;
        float finCounter = (bodyPitch - whale.getFinLag(partialTick)) * Mth.DEG_TO_RAD * FIN_COUNTER_GAIN;
        float droopF = whale.getFrontDroop(partialTick) * HEAD_DROOP_ANGLE;
        float droopB = whale.getBackDroop(partialTick) * TAIL_DROOP_ANGLE;

        this.rotateBone("root", -bodyPitch * Mth.DEG_TO_RAD, 0.0F, roll);
        this.bend("skullRot", whale, 0, partialTick);
        this.bend("skull", whale, 0, partialTick);
        this.bend("tail", whale, 1, partialTick);
        this.bend("tail2", whale, 2, partialTick);
        this.bend("fluke", whale, 3, partialTick);
        this.bend("flip", whale, 2, partialTick);
        this.rotateBone("leftFin", finCounter, 0.0F, 0.0F);
        this.rotateBone("rightFin", finCounter, 0.0F, 0.0F);
        this.rotateBone("skullRot", -droopF, 0.0F, -roll * HEAD_ROLL_STABILIZE);
        this.rotateBone("skull", -droopF, 0.0F, -roll * HEAD_ROLL_STABILIZE);
        this.rotateBone("tail", -droopB, 0.0F, 0.0F);
        this.rotateBone("tail2", -droopB, 0.0F, 0.0F);
        this.rotateBone("fluke", -droopB, 0.0F, roll * FLUKE_ROLL_FOLLOW);
        this.rotateBone("flip", -droopB, 0.0F, roll * FLUKE_ROLL_FOLLOW);
    }

    private void bend(String boneName, Whale whale, int segment, float partialTick) {
        this.rotateBone(boneName,
                -whale.getSegPitchOffset(segment, partialTick) * Mth.DEG_TO_RAD,
                YAW_GAIN * whale.getSegYawOffset(segment, partialTick) * Mth.DEG_TO_RAD,
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
