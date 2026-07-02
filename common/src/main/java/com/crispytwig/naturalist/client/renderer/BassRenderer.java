package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BassModel;
import com.crispytwig.naturalist.server.entity.mob.Bass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BassRenderer extends GeoEntityRenderer<Bass> {
    public BassRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BassModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public float getMotionAnimThreshold(Bass animatable) {
        return 0.000001f;
    }
}
