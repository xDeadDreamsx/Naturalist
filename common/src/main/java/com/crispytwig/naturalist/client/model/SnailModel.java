package com.crispytwig.naturalist.client.model;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.server.entity.climbing.SurfaceClimbing;
import com.crispytwig.naturalist.server.entity.mob.Snail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

@Environment(EnvType.CLIENT)
public class SnailModel extends GeoModel<Snail> {
    private static final float TAIL_STRENGTH = -1.4F;
    private static final float TAIL_MAX = 0.6F;
    private static final float TAIL_LOOK_STRENGTH = 3.0F;
    private static final float MAX_EYE_PITCH = Mth.DEG_TO_RAD * 22.5F;
    private static final float MAX_EYE_YAW = Mth.DEG_TO_RAD * 90.0F;

    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Snail snail) {
        return snail.getVariantModel(Naturalist.location("geo/entity/snail.geo.json"));
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(@NotNull Snail snail) {
        if (snail.hasNonDefaultVariant()) {
            return snail.getVariantTexture();
        }
        int color = snail.getSnailColor().getId();
        return Naturalist.location("textures/entity/snail/" + DyeColor.byId(color).getName() + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(Snail snail) {
        return snail.getVariantAnimation(Naturalist.location("animations/snail.animation.json"));
    }

    @Override
    public void setCustomAnimations(Snail animatable, long instanceId, @Nullable AnimationState<Snail> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        if (animationState == null) return;

        float eyeScale = animatable.isBaby() ? 1.5F : 1.0F;
        this.getBone("eyes").ifPresent(eyes -> {
            eyes.setScaleX(eyeScale);
            eyes.setScaleY(eyeScale);
            eyes.setScaleZ(eyeScale);
            eyes.resetStateChanges();
        });

        if (animatable.isHidden()) return;

        float partialTick = animationState.getPartialTick();
        float bodyLookYaw = animatable.getBodyLookYaw(partialTick);
        float bodyYaw = bodyLookYaw * Mth.DEG_TO_RAD;
        this.rotateBone("body", 0.0F, bodyYaw);
        this.rotateBone("shell", 0.0F, bodyYaw);

        SurfaceClimbing climbing = animatable.getClimbing();
        Vec3 normal = climbing.getRenderNormal(partialTick);
        Vec3 forward = climbing.getRenderForwardFlattened(partialTick, normal);
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vec3 toCamera = camera.getPosition().subtract(animatable.position());
        Vec3 plane = SurfaceClimbing.projectOntoPlane(toCamera, normal);
        double planeLen = plane.length();
        Vec3 cameraDir = planeLen < 1.0E-4D ? forward : plane.normalize();

        float eyeYaw = (float) Math.atan2(forward.cross(cameraDir).dot(normal), forward.dot(cameraDir)) - bodyYaw;
        eyeYaw = Mth.clamp(Mth.wrapDegrees(eyeYaw * Mth.RAD_TO_DEG) * Mth.DEG_TO_RAD, -MAX_EYE_YAW, MAX_EYE_YAW);
        float eyePitch = Mth.clamp((float) Math.atan2(toCamera.dot(normal), planeLen), -MAX_EYE_PITCH, MAX_EYE_PITCH);
        this.rotateBone("leftEye", eyePitch, eyeYaw);
        this.rotateBone("rightEye", eyePitch, eyeYaw);

        float tailXLag = Mth.clamp(climbing.getTailLag(partialTick) * TAIL_STRENGTH, -TAIL_MAX, TAIL_MAX);
        float tailYLag = (animatable.getTailLookYaw(partialTick) - bodyLookYaw) * TAIL_LOOK_STRENGTH * Mth.DEG_TO_RAD;
        this.rotateBone("tail", tailXLag, tailYLag);
    }

    private void rotateBone(String name, float pitch, float yaw) {
        this.getBone(name).ifPresent(bone -> {
            bone.setRotX(bone.getRotX() + pitch);
            bone.setRotY(bone.getRotY() + yaw);
            bone.resetStateChanges();
        });
    }
}
