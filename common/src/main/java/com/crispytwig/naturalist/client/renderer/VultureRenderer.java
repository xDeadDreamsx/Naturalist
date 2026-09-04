package com.crispytwig.naturalist.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.crispytwig.naturalist.client.model.VultureBabyModel;
import com.crispytwig.naturalist.client.model.VultureModel;
import com.crispytwig.naturalist.server.entity.mob.Vulture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class VultureRenderer extends NaturalistMobRenderer<Vulture> {
    public VultureRenderer(EntityRendererProvider.Context context) {
        super(context, new VultureModel(context.bakeLayer(VultureModel.LAYER_LOCATION)), new VultureBabyModel(context.bakeLayer(VultureBabyModel.LAYER_LOCATION)), 0.65F, 0.3F);
    }
}
