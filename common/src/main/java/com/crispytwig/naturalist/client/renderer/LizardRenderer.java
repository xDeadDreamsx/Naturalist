package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.client.model.LizardModel;
import com.crispytwig.naturalist.client.renderer.layers.DyeOverlayRenderLayer;
import com.crispytwig.naturalist.server.entity.mob.Lizard;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class LizardRenderer extends MobRenderer<Lizard, HierarchicalModel<Lizard>> {
    public LizardRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new LizardModel(context.bakeLayer(LizardModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new DyeOverlayRenderLayer<>(this, "lizard"));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Lizard entity) {
        return entity.getVariantTexture();
    }
}
