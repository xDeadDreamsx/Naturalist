package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.LizardModel;
import com.crispytwig.naturalist.client.renderer.layers.DyeOverlayLayer;
import com.crispytwig.naturalist.server.entity.mob.Lizard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class LizardRenderer extends GeoEntityRenderer<Lizard> {
    public LizardRenderer(EntityRendererProvider.@NotNull Context renderManager) {
        super(renderManager, new LizardModel());
        this.shadowRadius = 0.4F;
        this.addRenderLayer(new DyeOverlayLayer<>(this, "lizard"));
    }

    @Override
    public float getMotionAnimThreshold(Lizard animatable) {
        return 0.000001f;
    }
}
