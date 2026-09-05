package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistAvatarRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public abstract class AvatarRenderStateMixin implements NaturalistAvatarRenderState {
    @Unique
    private boolean naturalist$shoulderParrotsFlap;

    @Override
    public boolean naturalist$shouldShoulderParrotsFlap() {
        return this.naturalist$shoulderParrotsFlap;
    }

    @Override
    public void naturalist$setShoulderParrotsFlap(boolean flap) {
        this.naturalist$shoulderParrotsFlap = flap;
    }
}
