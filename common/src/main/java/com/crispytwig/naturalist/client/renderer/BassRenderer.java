package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.BassModel;
import com.crispytwig.naturalist.server.entity.mob.Bass;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class BassRenderer extends MobRenderer<Bass, HierarchicalModel<Bass>> {
    public BassRenderer(EntityRendererProvider.Context context) {
        super(context, new BassModel(context.bakeLayer(BassModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Bass entity) {
        return entity.getVariantTexture();
    }
}
