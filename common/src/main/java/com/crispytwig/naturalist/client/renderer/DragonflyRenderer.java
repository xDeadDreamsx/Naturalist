package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.DragonflyModel;
import com.crispytwig.naturalist.server.entity.mob.Dragonfly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class DragonflyRenderer extends GeoEntityRenderer<Dragonfly> {
    public DragonflyRenderer(EntityRendererProvider.@NotNull Context renderManager) {
        super(renderManager, new DragonflyModel());
        this.shadowRadius = 0.4F;
    }

    @Override
    public float getMotionAnimThreshold(Dragonfly animatable) {
        return 0.000001f;
    }
}
