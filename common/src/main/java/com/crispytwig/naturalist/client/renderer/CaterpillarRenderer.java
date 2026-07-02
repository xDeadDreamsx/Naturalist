package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.CaterpillarModel;
import com.crispytwig.naturalist.server.entity.mob.Caterpillar;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CaterpillarRenderer extends GeoEntityRenderer<Caterpillar> {
    public CaterpillarRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new CaterpillarModel());
        this.shadowRadius = 0.3F;
    }

    @Override
    public float getMotionAnimThreshold(Caterpillar animatable) {
        return 0.000001f;
    }
}
