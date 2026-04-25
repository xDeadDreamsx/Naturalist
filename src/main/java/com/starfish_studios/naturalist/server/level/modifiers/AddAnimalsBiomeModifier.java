package com.starfish_studios.naturalist.server.level.modifiers;

import com.mojang.serialization.MapCodec;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import com.starfish_studios.naturalist.registry.NaturalistBiomeModifiers;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.jetbrains.annotations.NotNull;

public class AddAnimalsBiomeModifier implements BiomeModifier {
    @Override
    public void modify(@NotNull Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.@NotNull Builder builder) {
        if (phase.equals(Phase.ADD)) {
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_ALLIGATOR, NaturalistTags.Biomes.BLACKLIST_ALLIGATOR, MobCategory.CREATURE, NaturalistEntityTypes.ALLIGATOR.get(), 10, 2, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_BASS, NaturalistTags.Biomes.BLACKLIST_BASS, MobCategory.WATER_AMBIENT, NaturalistEntityTypes.BASS.get(), 10, 3, 6);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_BEAR, NaturalistTags.Biomes.BLACKLIST_BEAR, MobCategory.CREATURE, NaturalistEntityTypes.BEAR.get(), 10, 1, 2);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_BLUEJAY, NaturalistTags.Biomes.BLACKLIST_BLUEJAY, MobCategory.CREATURE, NaturalistEntityTypes.BLUEJAY.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_BOAR, NaturalistTags.Biomes.BLACKLIST_BOAR, MobCategory.CREATURE, NaturalistEntityTypes.BOAR.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_BUTTERFLY, NaturalistTags.Biomes.BLACKLIST_BUTTERFLY, MobCategory.AMBIENT, NaturalistEntityTypes.BUTTERFLY.get(), 10, 3, 6);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_CANARY, NaturalistTags.Biomes.BLACKLIST_CANARY, MobCategory.CREATURE, NaturalistEntityTypes.CANARY.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_CARDINAL, NaturalistTags.Biomes.BLACKLIST_CARDINAL, MobCategory.CREATURE, NaturalistEntityTypes.CARDINAL.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_CATFISH, NaturalistTags.Biomes.BLACKLIST_CATFISH, MobCategory.WATER_AMBIENT, NaturalistEntityTypes.CATFISH.get(), 10, 1, 2);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_CORAL_SNAKE, NaturalistTags.Biomes.BLACKLIST_CORAL_SNAKE, MobCategory.CREATURE, NaturalistEntityTypes.CORAL_SNAKE.get(), 10, 1, 1);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_DEER, NaturalistTags.Biomes.BLACKLIST_DEER, MobCategory.CREATURE, NaturalistEntityTypes.DEER.get(), 10, 3, 5);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_DRAGONFLY, NaturalistTags.Biomes.BLACKLIST_DRAGONFLY, MobCategory.AMBIENT, NaturalistEntityTypes.DRAGONFLY.get(), 10, 2, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_DUCK, NaturalistTags.Biomes.BLACKLIST_DUCK, MobCategory.CREATURE, NaturalistEntityTypes.DUCK.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_ELEPHANT, NaturalistTags.Biomes.BLACKLIST_ELEPHANT, MobCategory.CREATURE, NaturalistEntityTypes.ELEPHANT.get(), 5, 1, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_FINCH, NaturalistTags.Biomes.BLACKLIST_FINCH, MobCategory.CREATURE, NaturalistEntityTypes.FINCH.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_FIREFLY, NaturalistTags.Biomes.BLACKLIST_FIREFLY, MobCategory.AMBIENT, NaturalistEntityTypes.FIREFLY.get(), 10, 2, 4);
            addMobSpawn(builder, biome, BiomeTags.IS_FOREST, MobCategory.CREATURE, EntityType.FOX, 10, 1, 2);
            addMobSpawn(builder, biome, BiomeTags.IS_FOREST, MobCategory.CREATURE, EntityType.RABBIT, 10, 2, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_GIRAFFE, NaturalistTags.Biomes.BLACKLIST_GIRAFFE, MobCategory.CREATURE, NaturalistEntityTypes.GIRAFFE.get(), 5, 1, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_HIPPO, NaturalistTags.Biomes.BLACKLIST_HIPPO, MobCategory.CREATURE, NaturalistEntityTypes.HIPPO.get(), 10, 1, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_LION, NaturalistTags.Biomes.BLACKLIST_LION, MobCategory.CREATURE, NaturalistEntityTypes.LION.get(), 3, 1, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_LIZARD, NaturalistTags.Biomes.BLACKLIST_LIZARD, MobCategory.CREATURE, NaturalistEntityTypes.LIZARD.get(), 10, 1, 1);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_RHINO, NaturalistTags.Biomes.BLACKLIST_RHINO, MobCategory.CREATURE, NaturalistEntityTypes.RHINO.get(), 1, 1, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_ROBIN, NaturalistTags.Biomes.BLACKLIST_ROBIN, MobCategory.CREATURE, NaturalistEntityTypes.ROBIN.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_SNAIL, NaturalistTags.Biomes.BLACKLIST_SNAIL, MobCategory.CREATURE, NaturalistEntityTypes.SNAIL.get(), 10, 2, 3);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_SNAKE, NaturalistTags.Biomes.BLACKLIST_SNAKE, MobCategory.CREATURE, NaturalistEntityTypes.SNAKE.get(), 10, 1, 1);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_SPARROW, NaturalistTags.Biomes.BLACKLIST_SPARROW, MobCategory.CREATURE, NaturalistEntityTypes.SPARROW.get(), 10, 3, 4);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_TORTOISE, NaturalistTags.Biomes.BLACKLIST_TORTOISE, MobCategory.CREATURE, NaturalistEntityTypes.TORTOISE.get(), 10, 1, 1);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_VULTURE, NaturalistTags.Biomes.BLACKLIST_VULTURE, MobCategory.CREATURE, NaturalistEntityTypes.VULTURE.get(), 3, 3, 5);
            addIfValid(builder, biome, NaturalistTags.Biomes.HAS_ZEBRA, NaturalistTags.Biomes.BLACKLIST_ZEBRA, MobCategory.CREATURE, NaturalistEntityTypes.ZEBRA.get(), 1, 2, 6);
        }
    }

    private void addIfValid(ModifiableBiomeInfo.BiomeInfo.Builder builder, Holder<Biome> biome, TagKey<Biome> hasTag, TagKey<Biome> blacklistTag, MobCategory category, EntityType<?> entityType, int weight, int min, int max) {
        if (biome.is(hasTag) && !biome.is(blacklistTag)) {
            builder.getMobSpawnSettings().addSpawn(category, new MobSpawnSettings.SpawnerData(entityType, weight, min, max));
        }
    }

    private void addMobSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, Holder<Biome> biome, TagKey<Biome> tag, MobCategory category, EntityType<?> entityType, int weight, int min, int max) {
        if (biome.is(tag)) {
            builder.getMobSpawnSettings().addSpawn(category, new MobSpawnSettings.SpawnerData(entityType, weight, min, max));
        }
    }

    @Override
    public @NotNull MapCodec<? extends BiomeModifier> codec() {
        return NaturalistBiomeModifiers.ADD_ANIMALS_CODEC.get();
    }
}
