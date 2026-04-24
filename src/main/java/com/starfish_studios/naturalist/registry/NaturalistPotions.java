package com.starfish_studios.naturalist.registry;

import com.starfish_studios.naturalist.Naturalist;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NaturalistPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, Naturalist.MOD_ID);

    public static final DeferredHolder<Potion, Potion> FOREST_DASHER = POTIONS.register("forest_dasher",
            () -> new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1), new MobEffectInstance(MobEffects.WEAKNESS, 400, 0)));

    public static final DeferredHolder<Potion, Potion> LONG_FOREST_DASHER = POTIONS.register("long_forest_dasher",
            () -> new Potion("forest_dasher", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 1), new MobEffectInstance(MobEffects.WEAKNESS, 800, 0)));

    public static final DeferredHolder<Potion, Potion> STRONG_FOREST_DASHER = POTIONS.register("strong_forest_dasher",
            () -> new Potion("forest_dasher", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2), new MobEffectInstance(MobEffects.WEAKNESS, 400, 1)));
}
