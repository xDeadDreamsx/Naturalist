package com.starfish_studios.naturalist.fabric.platform;

import com.starfish_studios.naturalist.platform.services.IRegistryHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {
    @Override
    public SpawnEggItem createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties) {
        return new SpawnEggItem(type.get(), primaryColor, secondaryColor, properties);
    }
}
