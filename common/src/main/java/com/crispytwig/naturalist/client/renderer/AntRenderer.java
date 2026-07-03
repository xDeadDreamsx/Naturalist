package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.AntModel;
import com.crispytwig.naturalist.server.entity.mob.Ant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class AntRenderer extends GeoEntityRenderer<Ant> {
    public AntRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AntModel());
        this.shadowRadius = 0.25F;
    }

    @Override
    public float getMotionAnimThreshold(Ant animatable) {
        return 0.000001f;
    }
}
