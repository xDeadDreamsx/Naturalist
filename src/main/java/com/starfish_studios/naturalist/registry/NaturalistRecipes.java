package com.starfish_studios.naturalist.registry;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.recipe.BugNetInteractionRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NaturalistRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Naturalist.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Naturalist.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<BugNetInteractionRecipe>> BUG_NET = RECIPE_TYPES.register("net",
            () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return "naturalist:net";
                }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> BUG_NET_SERIALIZER = RECIPE_SERIALIZERS.register("net",
            BugNetInteractionRecipe.Serializer::new);
}
