package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.variant.DataDrivenVariantAnimal;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

/** Shared renderer for Naturalist mobs on Minecraft's render-state based renderer. */
@Environment(EnvType.CLIENT)
public abstract class NaturalistMobRenderer<T extends Mob & DataDrivenVariantAnimal>
        extends AgeableMobRenderer<T, NaturalistRenderState<T>, NaturalistEntityModel<T>> {
    private final float adultShadowRadius;
    private final float babyShadowRadius;

    protected NaturalistMobRenderer(EntityRendererProvider.Context context,
                                    NaturalistEntityModel<T> adultModel,
                                    NaturalistEntityModel<T> babyModel,
                                    float shadowRadius) {
        this(context, adultModel, babyModel, shadowRadius, shadowRadius / 2.0F);
    }

    protected NaturalistMobRenderer(EntityRendererProvider.Context context,
                                    NaturalistEntityModel<T> adultModel,
                                    NaturalistEntityModel<T> babyModel,
                                    float shadowRadius,
                                    float babyShadowRadius) {
        super(context, adultModel, babyModel, shadowRadius);
        this.adultShadowRadius = shadowRadius;
        this.babyShadowRadius = babyShadowRadius;
    }

    @Override
    public @NotNull Identifier getTextureLocation(@NotNull NaturalistRenderState<T> state) {
        T entity = state.entity;
        if (entity == null) {
            return Identifier.withDefaultNamespace("textures/missing.png");
        }
        return state.isBaby ? entity.getVariantBabyTexture() : entity.getVariantTexture();
    }

    @Override
    protected float getShadowRadius(NaturalistRenderState<T> state) {
        return state.isBaby ? this.babyShadowRadius : this.adultShadowRadius;
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
