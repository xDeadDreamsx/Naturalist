package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.SnakeModel;
import com.crispytwig.naturalist.server.entity.mob.Snake;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class SnakeRenderer extends MobRenderer<Snake, HierarchicalModel<Snake>> {
    public SnakeRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new SnakeModel(context.bakeLayer(SnakeModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Snake entity) {
        return entity.getVariantTexture();
    }
}
