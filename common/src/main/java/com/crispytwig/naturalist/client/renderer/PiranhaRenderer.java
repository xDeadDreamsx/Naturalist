package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.PiranhaModel;
import com.crispytwig.naturalist.server.entity.mob.Piranha;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class PiranhaRenderer extends MobRenderer<Piranha, HierarchicalModel<Piranha>> {
    public PiranhaRenderer(EntityRendererProvider.Context context) {
        super(context, new PiranhaModel(context.bakeLayer(PiranhaModel.LAYER_LOCATION)), 0.0F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Piranha entity) {
        return entity.getVariantTexture();
    }
}
