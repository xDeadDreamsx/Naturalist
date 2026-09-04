package com.crispytwig.naturalist.client.renderer.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** Compatibility render state used while Naturalist's entity animations are migrated to 26.x. */
public class NaturalistRenderState<E extends Entity> extends LivingEntityRenderState {
    @Nullable
    public E entity;
    public float partialTick;
}
