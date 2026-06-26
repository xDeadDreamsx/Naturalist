package com.crispytwig.naturalist.neoforge.platform;

import com.crispytwig.naturalist.platform.services.IRegistryHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    @Override
    public SpawnEggItem createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties) {
        return new DeferredSpawnEggItem(type, primaryColor, secondaryColor, properties);
    }
}
