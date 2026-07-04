package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.JungleScorpionModel;
import com.crispytwig.naturalist.server.entity.mob.JungleScorpion;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class JungleScorpionRenderer extends GeoEntityRenderer<JungleScorpion> {
    public JungleScorpionRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new JungleScorpionModel());
        this.shadowRadius = 0.7F;
    }

    @Override
    public float getMotionAnimThreshold(JungleScorpion animatable) {
        return 0.000001f;
    }
}
