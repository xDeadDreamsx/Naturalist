package com.crispytwig.naturalist.fabric;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.fabric.config.FabricNaturalistConfig;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.level.NaturalistSpawns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;
import java.util.function.Predicate;

public class NaturalistFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricNaturalistConfig.load();
        Naturalist.bootstrap();

        Naturalist.createAttributes((type, builder) -> FabricDefaultAttributeRegistry.register(type, builder.build()));

        Naturalist.registerSpawnPlacements(new Naturalist.SpawnPlacementRegistrar() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                SpawnPlacements.register(type, placementType, heightmap, predicate);
            }
        });

        Naturalist.registerDispenserBehaviors();

        FabricBrewingRecipeRegistryBuilder.BUILD.register(builder ->
                Naturalist.registerPotionMixes((input, ingredient, output) ->
                        builder.registerPotionRecipe(input, Ingredient.of(ingredient), output)));

        registerBiomeSpawns();
        registerBiomeFeatures();
    }

    private static void registerBiomeSpawns() {
        NaturalistSpawns.forEachSpawn((hasTag, blacklistTag, category, type, weight, min, max) -> {
            Predicate<BiomeSelectionContext> selector = ctx ->
                    ctx.hasTag(hasTag) && (blacklistTag == null || !ctx.hasTag(blacklistTag));
            BiomeModifications.addSpawn(selector, category, type, weight, min, max);
        });
    }

    private static void registerBiomeFeatures() {
        for (String name : List.of("ant_hill_small", "ant_hill_big")) {
            BiomeModifications.addFeature(ctx -> ctx.hasTag(NaturalistTags.Biomes.HAS_ANT_HILL), GenerationStep.Decoration.SURFACE_STRUCTURES,
                    ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, name)));
        }
    }
}
