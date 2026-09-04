package com.crispytwig.naturalist.fabric;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.NaturalistConfig;
import com.crispytwig.naturalist.fabric.config.FabricNaturalistConfig;
import com.crispytwig.naturalist.registry.NaturalistTags;
import com.crispytwig.naturalist.server.level.NaturalistSpawns;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.item.crafting.Ingredient;

public class NaturalistFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricNaturalistConfig.load();
        Naturalist.bootstrap();

        Naturalist.createAttributes((type, builder) -> {
            // TemptGoal requires minecraft:tempt_range in 26.2. Naturalist 2.0.3
            // predates that attribute, so make it available to the port's mobs.
            if (!builder.hasAttribute(Attributes.TEMPT_RANGE)) {
                builder.add(Attributes.TEMPT_RANGE, 10.0D);
            }
            FabricDefaultAttributeRegistry.register(type, builder.build());
        });

        Naturalist.registerSpawnPlacements(SpawnPlacements::register);

        Naturalist.registerDispenserBehaviors();

        FabricPotionBrewingBuilder.BUILD.register(builder ->
                Naturalist.registerPotionMixes((from, ingredient, to) -> builder.registerPotionRecipe(from, Ingredient.of(ingredient), to)));

        registerBiomeSpawns();
        registerBiomeFeatures();
    }

    private static void registerBiomeSpawns() {
        NaturalistSpawns.forEachSpawn((hasTag, blacklistTag, category, type, weight, min, max) -> {
            BiomeModifications.addSpawn(ctx -> !NaturalistConfig.isRemoved(type)
                    && ctx.hasTag(hasTag) && (blacklistTag == null || !ctx.hasTag(blacklistTag)), category, type, weight, min, max);
        });
    }

    private static void registerBiomeFeatures() {
        for (String name : List.of("ant_hill_small", "ant_hill_big")) {
            BiomeModifications.addFeature(ctx -> ctx.hasTag(NaturalistTags.Biomes.HAS_ANT_HILL), GenerationStep.Decoration.SURFACE_STRUCTURES,
                    ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath(Naturalist.MOD_ID, name)));
        }
    }
}
