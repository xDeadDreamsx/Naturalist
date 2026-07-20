package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.RayModel;
import com.crispytwig.naturalist.server.entity.mob.Ray;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class RayRenderer extends MobRenderer<Ray, HierarchicalModel<Ray>> {
    public RayRenderer(EntityRendererProvider.Context context) {
        super(context, new RayModel(context.bakeLayer(RayModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Ray entity) {
        return entity.getVariantTexture();
    }
}
