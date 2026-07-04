package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.server.entity.mob.Ostrich;
import net.minecraft.world.item.DyeColor;
import software.bernie.geckolib.renderer.GeoRenderer;

public class OstrichDyeOverlayLayer extends DyeOverlayLayer<Ostrich> {
    public OstrichDyeOverlayLayer(GeoRenderer<Ostrich> renderer) {
        super(renderer, "ostrich");
    }

    @Override
    protected String textureName(Ostrich entity, DyeColor color) {
        return color.getName() + (entity.isBaby() ? "_baby" : "");
    }
}
