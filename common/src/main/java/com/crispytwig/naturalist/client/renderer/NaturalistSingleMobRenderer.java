package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/** Renderer adapter for Naturalist mobs that use one model for all ages. */
@Environment(EnvType.CLIENT)
public class NaturalistSingleMobRenderer<T extends Mob & DataDrivenVariantAnimal>
        extends MobRenderer<T, NaturalistRenderState<T>, NaturalistEntityModel<T>> {

    protected NaturalistSingleMobRenderer(EntityRendererProvider.Context context, NaturalistEntityModel<T> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull NaturalistRenderState<T> state) {
        T entity = state.entity;
        return entity != null ? entity.getVariantTexture() : Identifier.withDefaultNamespace("textures/missing.png");
    }

    @Override
    public NaturalistRenderState<T> createRenderState() {
        return new NaturalistRenderState<>();
    }

    @Override
    public void extractRenderState(T entity, NaturalistRenderState<T> state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.entity = entity;
        state.partialTick = partialTick;
    }
}
