package com.starfish_studios.naturalist.client.renderer;

import com.starfish_studios.naturalist.client.model.SnakeModel;
import com.starfish_studios.naturalist.server.entity.mob.Snake;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class SnakeRenderer extends GeoEntityRenderer<Snake> {
    public SnakeRenderer(EntityRendererProvider.@NotNull Context renderManager) {
        super(renderManager, new SnakeModel());
        this.shadowRadius = 0.4F;
    }

    @SuppressWarnings("unused")
    @Override
    public float getMotionAnimThreshold(Snake animatable) {
        return 0.000001f;
    }
}
