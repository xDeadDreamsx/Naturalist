package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.DesertScorpionModel;
import com.crispytwig.naturalist.server.entity.mob.DesertScorpion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DesertScorpionRenderer extends GeoEntityRenderer<DesertScorpion> {
    public DesertScorpionRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DesertScorpionModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public float getMotionAnimThreshold(DesertScorpion animatable) {
        return 0.000001f;
    }
}
