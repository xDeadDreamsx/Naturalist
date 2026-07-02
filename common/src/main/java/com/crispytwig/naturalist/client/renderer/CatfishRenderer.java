package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.CatfishModel;
import com.crispytwig.naturalist.server.entity.mob.Catfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CatfishRenderer extends GeoEntityRenderer<Catfish> {
    public CatfishRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CatfishModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public float getMotionAnimThreshold(Catfish animatable) {
        return 0.000001f;
    }
}
