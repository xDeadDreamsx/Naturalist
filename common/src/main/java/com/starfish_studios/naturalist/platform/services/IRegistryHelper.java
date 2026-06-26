package com.starfish_studios.naturalist.platform.services;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

import java.util.function.Supplier;

public interface IRegistryHelper {
    SpawnEggItem createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties);
}
