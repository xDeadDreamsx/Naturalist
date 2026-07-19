package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.ClamModel;
import com.crispytwig.naturalist.client.renderer.layers.ClamItemLayer;
import com.crispytwig.naturalist.server.entity.mob.Clam;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ClamRenderer extends MobRenderer<Clam, HierarchicalModel<Clam>> {
    public ClamRenderer(EntityRendererProvider.Context context) {
        super(context, new ClamModel(context.bakeLayer(ClamModel.LAYER_LOCATION)), 0.6F);
        this.addLayer(new ClamItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Clam entity) {
        return entity.getVariantTexture();
    }
}
