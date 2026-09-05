package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistWolfRenderState;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WolfModel.class)
public abstract class WolfModelMixin {
    @Shadow @Final protected ModelPart head;
    @Shadow @Final protected ModelPart body;
    @Shadow @Final protected ModelPart tail;
    @Shadow @Final protected ModelPart leftFrontLeg;
    @Shadow @Final protected ModelPart rightFrontLeg;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void naturalist$digAnim(WolfRenderState state, CallbackInfo ci) {
        if (!((NaturalistWolfRenderState) state).naturalist$isDiggingOutMole()) {
            return;
        }

        float scratch = Mth.cos(state.ageInTicks * 1.1F);
        this.rightFrontLeg.xRot = scratch * 0.9F;
        this.leftFrontLeg.xRot = -scratch * 0.9F;
        this.head.xRot = Math.max(this.head.xRot, 0.85F);

        float crouch = 1.5F * state.ageScale;
        this.body.y += crouch;
        this.tail.y += crouch;
        this.head.y += crouch;

        float tilt = 0.15F;
        this.body.xRot += tilt;
        this.tail.xRot -= tilt;
    }
}
