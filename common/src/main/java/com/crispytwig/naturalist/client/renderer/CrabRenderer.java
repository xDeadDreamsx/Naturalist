package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.CrabModel;
import com.crispytwig.naturalist.client.renderer.layers.CrabItemLayer;
import com.crispytwig.naturalist.server.entity.mob.Crab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CrabRenderer extends GeoEntityRenderer<Crab> {
    public CrabRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CrabModel());
        this.shadowRadius = 0.3F;
        this.addRenderLayer(new CrabItemLayer(this));
    }

    @Override
    public float getMotionAnimThreshold(Crab animatable) {
        return 0.000001f;
    }
}
