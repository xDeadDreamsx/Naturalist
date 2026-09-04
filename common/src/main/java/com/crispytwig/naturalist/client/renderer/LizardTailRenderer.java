package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.crispytwig.naturalist.client.model.LizardTailModel;
import com.crispytwig.naturalist.server.entity.mob.LizardTail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class LizardTailRenderer extends NaturalistSingleMobRenderer<LizardTail> {
    public LizardTailRenderer(EntityRendererProvider.Context context) {
        super(context, new LizardTailModel(context.bakeLayer(LizardTailModel.LAYER_LOCATION)), 0.4F);
    }
}
