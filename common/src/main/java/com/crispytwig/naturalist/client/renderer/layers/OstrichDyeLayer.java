package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.client.model.NaturalistEntityModel;
import com.crispytwig.naturalist.client.renderer.state.NaturalistRenderState;
import com.crispytwig.naturalist.server.entity.mob.Ostrich;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;

import java.util.EnumMap;

public class OstrichDyeLayer extends DyeLayer<Ostrich, NaturalistEntityModel<Ostrich>> {
    private final EnumMap<DyeColor, Identifier> babyTextures = new EnumMap<>(DyeColor.class);

    public OstrichDyeLayer(RenderLayerParent<NaturalistRenderState<Ostrich>, NaturalistEntityModel<Ostrich>> parent) {
        super(parent, "ostrich");
    }

    @Override
    protected Identifier getDyeTexture(Ostrich entity, DyeColor color) {
        if (!entity.isBaby()) return super.getDyeTexture(entity, color);
        return this.babyTextures.computeIfAbsent(color, c -> this.getDyeTexture(c.getName() + "_baby"));
    }
}
