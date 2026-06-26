package com.starfish_studios.naturalist.neoforge.platform;

import com.starfish_studios.naturalist.platform.registry.DeferredHolder;
import com.starfish_studios.naturalist.platform.registry.RegistrationProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NeoForgeRegistrationProvider<T> implements RegistrationProvider<T> {
    public static IEventBus EVENT_BUS;

    private final DeferredRegister<T> registry;

    public NeoForgeRegistrationProvider(ResourceKey<? extends Registry<T>> registryKey, String modId) {
        if (EVENT_BUS == null) {
            throw new IllegalStateException("Naturalist ModEventBus was not set before registry creation!");
        }
        this.registry = DeferredRegister.create(registryKey, modId);
        this.registry.register(EVENT_BUS);
    }

    @Override
    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<I> supplier) {
        var holder = registry.register(name, supplier);
        return new DeferredHolder<>(holder);
    }

    public static class Factory implements RegistrationProvider.Factory {
        @Override
        public <T> RegistrationProvider<T> create(ResourceKey<? extends Registry<T>> registryKey, String modId) {
            return new NeoForgeRegistrationProvider<>(registryKey, modId);
        }
    }
}
