package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.ButterflyModel;
import com.crispytwig.naturalist.server.entity.mob.Butterfly;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class ButterflyRenderer extends MobRenderer<Butterfly, HierarchicalModel<Butterfly>> {
    public ButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new ButterflyModel(context.bakeLayer(ButterflyModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Butterfly entity) {
        return entity.getVariantTexture();
    }
}
