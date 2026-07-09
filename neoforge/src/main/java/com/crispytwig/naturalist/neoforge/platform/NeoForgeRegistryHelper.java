package com.crispytwig.naturalist.neoforge.platform;

import com.crispytwig.naturalist.platform.services.IRegistryHelper;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {
    @Override
    public SpawnEggItem createSpawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Item.Properties properties) {
        return new DeferredSpawnEggItem(type, primaryColor, secondaryColor, properties);
    }

    @Override
    public <T> void registerSyncedDataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
        if (NeoForgeRegistrationProvider.EVENT_BUS == null) {
            throw new IllegalStateException("Naturalist ModEventBus was not set before datapack registry!");
        }
        NeoForgeRegistrationProvider.EVENT_BUS.addListener((DataPackRegistryEvent.NewRegistry event) ->
                event.dataPackRegistry(registryKey, codec, codec));
    }
}
