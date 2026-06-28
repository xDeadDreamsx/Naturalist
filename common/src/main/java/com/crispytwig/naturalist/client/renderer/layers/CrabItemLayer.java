package com.crispytwig.naturalist.client.renderer.layers;

import com.crispytwig.naturalist.server.entity.mob.Crab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

@Environment(EnvType.CLIENT)
public class CrabItemLayer extends BlockAndItemGeoLayer<Crab> {
    public CrabItemLayer(GeoRenderer<Crab> renderer) {
        super(renderer);
    }

    @Nullable
    @Override
    protected ItemStack getStackForBone(GeoBone bone, Crab crab) {
        ItemStack held = crab.getMainHandItem();
        return (bone.getName().equals("rightItem") && !held.isEmpty()) ? held : null;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, Crab crab) {
        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
