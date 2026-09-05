package com.crispytwig.naturalist.mixin;

import com.crispytwig.naturalist.client.renderer.state.NaturalistWolfRenderState;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WolfRenderState.class)
public abstract class WolfRenderStateMixin implements NaturalistWolfRenderState {
    @Unique
    private boolean naturalist$diggingOutMole;

    @Override
    public boolean naturalist$isDiggingOutMole() {
        return this.naturalist$diggingOutMole;
    }

    @Override
    public void naturalist$setDiggingOutMole(boolean digging) {
        this.naturalist$diggingOutMole = digging;
    }
}
