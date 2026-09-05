package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistAvatarRenderState;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.parrot.Parrot;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ParrotOnShoulderLayer.class)
public abstract class ParrotOnShoulderLayerMixin {
    @Redirect(
            method = "submitOnShoulder",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/entity/state/ParrotRenderState;pose:Lnet/minecraft/client/model/animal/parrot/ParrotModel$Pose;",
                    opcode = org.objectweb.asm.Opcodes.PUTFIELD)
    )
    private void naturalist$shoulderFlightPose(
            ParrotRenderState parrotState,
            ParrotModel.Pose originalPose,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            int lightCoords,
            AvatarRenderState playerState,
            Parrot.Variant parrotVariant,
            float yRot,
            float xRot,
            boolean isLeft) {
        if (((NaturalistAvatarRenderState) playerState).naturalist$shouldShoulderParrotsFlap()) {
            parrotState.pose = ParrotModel.Pose.FLYING;
            parrotState.flapAngle = Mth.sin(playerState.ageInTicks * 1.5F) + 1.0F;
        } else {
            parrotState.pose = originalPose;
        }
    }
}
