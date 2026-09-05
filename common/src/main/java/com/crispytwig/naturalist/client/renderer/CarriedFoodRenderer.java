package com.crispytwig.naturalist.client.renderer;

import com.crispytwig.naturalist.server.entity.misc.CarriedFoodEntity;
import com.crispytwig.naturalist.server.entity.mob.Ant;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class CarriedFoodRenderer extends ItemEntityRenderer {
    public CarriedFoodRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void extractRenderState(ItemEntity entity, ItemEntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        if (entity instanceof CarriedFoodEntity food) {
            Ant ant = food.resolveAnt();
            if (ant != null) {
                state.x = Mth.lerp(partialTick, ant.xOld, ant.getX());
                state.y = Mth.lerp(partialTick, ant.yOld, ant.getY()) + ant.getBbHeight() + CarriedFoodEntity.BACK_GAP;
                state.z = Mth.lerp(partialTick, ant.zOld, ant.getZ());
            }
        }
    }
}
