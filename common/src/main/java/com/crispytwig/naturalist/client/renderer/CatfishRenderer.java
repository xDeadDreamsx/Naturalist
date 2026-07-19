package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.CatfishModel;
import com.crispytwig.naturalist.server.entity.mob.Catfish;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CatfishRenderer extends MobRenderer<Catfish, HierarchicalModel<Catfish>> {
    public CatfishRenderer(EntityRendererProvider.Context context) {
        super(context, new CatfishModel(context.bakeLayer(CatfishModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Catfish entity) {
        return entity.getVariantTexture();
    }
}
