package com.starfish_studios.naturalist.datagen;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NaturalistBiomeTagsProvider extends TagsProvider<Biome> {
    private static final ResourceLocation ATMOSPHERIC_IS_RAINFOREST = ResourceLocation.fromNamespaceAndPath("atmospheric", "is_rainforest");
    private static final ResourceLocation ATMOSPHERIC_KOUSA_JUNGLE = ResourceLocation.fromNamespaceAndPath("atmospheric", "kousa_jungle");

    public NaturalistBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BIOME, lookupProvider, Naturalist.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(NaturalistTags.Biomes.HAS_ALLIGATOR)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP).add(Biomes.RIVER)
                .addOptionalTag(Tags.Biomes.IS_SWAMP)
                .addOptionalTag(ATMOSPHERIC_IS_RAINFOREST);

        tag(NaturalistTags.Biomes.HAS_BASS)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .addTag(BiomeTags.IS_RIVER)
                .addOptionalTag(Tags.Biomes.IS_SWAMP);

        tag(NaturalistTags.Biomes.HAS_BEAR)
                .addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_TAIGA)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_TAIGA);

        tag(NaturalistTags.Biomes.HAS_BLUEJAY)
                .addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_HILL)
                .add(Biomes.ICE_SPIKES).add(Biomes.SNOWY_PLAINS).add(Biomes.SNOWY_SLOPES)
                .addOptionalTag(Tags.Biomes.IS_SNOWY).addOptionalTag(Tags.Biomes.IS_MOUNTAIN)
                .addOptionalTag(Tags.Biomes.IS_CONIFEROUS_TREE).addOptionalTag(Tags.Biomes.IS_TAIGA)
                .addOptionalTag(Tags.Biomes.IS_ICY).addOptionalTag(Tags.Biomes.IS_HILL)
                .addOptional(ATMOSPHERIC_KOUSA_JUNGLE);

        tag(NaturalistTags.Biomes.HAS_BOAR)
                .addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_FOREST)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA);

        tag(NaturalistTags.Biomes.HAS_BUTTERFLY)
                .addTag(BiomeTags.IS_FOREST)
                .add(Biomes.PLAINS).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .addOptionalTag(Tags.Biomes.IS_SWAMP)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_PLAINS);

        tag(NaturalistTags.Biomes.HAS_CANARY)
                .addTag(BiomeTags.IS_HILL).addTag(BiomeTags.IS_MOUNTAIN)
                .addOptionalTag(Tags.Biomes.IS_MOUNTAIN).addOptionalTag(Tags.Biomes.IS_HILL);

        tag(NaturalistTags.Biomes.HAS_CARDINAL)
                .addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_SAVANNA)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP).add(Biomes.DESERT)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_SANDY)
                .addOptionalTag(Tags.Biomes.IS_DESERT);

        tag(NaturalistTags.Biomes.HAS_CATFISH)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .addOptionalTag(Tags.Biomes.IS_SWAMP);

        tag(NaturalistTags.Biomes.HAS_CORAL_SNAKE)
                .addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_RIVER)
                .add(Biomes.BEACH).add(Biomes.STONY_SHORE)
                .addOptionalTag(Tags.Biomes.IS_BEACH).addOptionalTag(Tags.Biomes.IS_JUNGLE)
                .addOptionalTag(Tags.Biomes.IS_RIVER);

        tag(NaturalistTags.Biomes.HAS_DEER)
                .addTag(BiomeTags.IS_FOREST).add(Biomes.CHERRY_GROVE)
                .addOptionalTag(Tags.Biomes.IS_FOREST);

        tag(NaturalistTags.Biomes.HAS_DRAGONFLY)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .addOptionalTag(Tags.Biomes.IS_SWAMP);

        tag(NaturalistTags.Biomes.HAS_DUCK)
                .add(Biomes.SWAMP).addTag(BiomeTags.IS_RIVER)
                .addOptionalTag(Tags.Biomes.IS_SWAMP);

        tag(NaturalistTags.Biomes.HAS_ELEPHANT)
                .addTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA);

        tag(NaturalistTags.Biomes.HAS_FINCH)
                .addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_FOREST)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_SAVANNA);

        tag(NaturalistTags.Biomes.HAS_FIREFLY)
                .addTag(BiomeTags.IS_FOREST)
                .add(Biomes.PLAINS).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP).add(Biomes.MUSHROOM_FIELDS)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_PLAINS)
                .addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_MUSHROOM);

        tag(NaturalistTags.Biomes.HAS_GIRAFFE)
                .addTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA);

        tag(NaturalistTags.Biomes.HAS_HIPPO)
                .addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_JUNGLE)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA).addOptionalTag(Tags.Biomes.IS_JUNGLE)
                .addOptionalTag(ATMOSPHERIC_IS_RAINFOREST);

        tag(NaturalistTags.Biomes.HAS_LION)
                .addTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA);

        tag(NaturalistTags.Biomes.HAS_LIZARD)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP).add(Biomes.DESERT)
                .addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_DESERT)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_JUNGLE);

        tag(NaturalistTags.Biomes.HAS_RATTLESNAKE)
                .addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_SAVANNA)
                .add(Biomes.DESERT)
                .addOptionalTag(Tags.Biomes.IS_SANDY).addOptionalTag(Tags.Biomes.IS_SAVANNA).addOptionalTag(Tags.Biomes.IS_DESERT);

        tag(NaturalistTags.Biomes.HAS_RHINO)
                .addTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA);

        tag(NaturalistTags.Biomes.HAS_ROBIN)
                .addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_MOUNTAIN)
                .add(Biomes.PLAINS).add(Biomes.CHERRY_GROVE)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_MOUNTAIN).addOptionalTag(Tags.Biomes.IS_PLAINS);

        tag(NaturalistTags.Biomes.HAS_SNAIL)
                .addTag(BiomeTags.IS_FOREST).addTag(BiomeTags.IS_SAVANNA)
                .addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_HILL).addTag(BiomeTags.IS_MOUNTAIN)
                .add(Biomes.PLAINS).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .add(Biomes.LUSH_CAVES).add(Biomes.DRIPSTONE_CAVES).add(Biomes.MUSHROOM_FIELDS)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_RIVER).addOptionalTag(Tags.Biomes.IS_HILL)
                .addOptionalTag(Tags.Biomes.IS_MOUNTAIN).addOptionalTag(Tags.Biomes.IS_PLAINS)
                .addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_UNDERGROUND).addOptionalTag(Tags.Biomes.IS_MUSHROOM);

        tag(NaturalistTags.Biomes.HAS_SNAKE)
                .addTag(BiomeTags.IS_FOREST)
                .add(Biomes.PLAINS).add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP)
                .addOptionalTag(Tags.Biomes.IS_FOREST).addOptionalTag(Tags.Biomes.IS_PLAINS).addOptionalTag(Tags.Biomes.IS_SWAMP);

        tag(NaturalistTags.Biomes.HAS_SPARROW)
                .add(Biomes.PLAINS).add(Biomes.CHERRY_GROVE)
                .addOptionalTag(Tags.Biomes.IS_PLAINS);

        tag(NaturalistTags.Biomes.HAS_TORTOISE)
                .add(Biomes.SWAMP).add(Biomes.MANGROVE_SWAMP).add(Biomes.DESERT)
                .addTag(BiomeTags.IS_JUNGLE)
                .addOptionalTag(Tags.Biomes.IS_SWAMP).addOptionalTag(Tags.Biomes.IS_DESERT)
                .addOptionalTag(Tags.Biomes.IS_JUNGLE);

        tag(NaturalistTags.Biomes.HAS_VULTURE)
                .addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_BADLANDS)
                .add(Biomes.DESERT)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA).addOptionalTag(Tags.Biomes.IS_DESERT);

        tag(NaturalistTags.Biomes.HAS_ZEBRA)
                .addTag(BiomeTags.IS_SAVANNA)
                .addOptionalTag(Tags.Biomes.IS_SAVANNA);

        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_ALLIGATOR);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_BASS);
        tag(NaturalistTags.Biomes.BLACKLIST_BEAR)
                .addOptionalTag(Tags.Biomes.IS_HOT).addOptionalTag(ATMOSPHERIC_IS_RAINFOREST);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_BLUEJAY);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_BOAR);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_BUTTERFLY);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_CANARY);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_CARDINAL);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_CATFISH);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_CORAL_SNAKE);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_DEER);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_DRAGONFLY);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_DUCK);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_ELEPHANT);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_FINCH);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_FIREFLY);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_FOREST_FOXES);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_FOREST_RABBITS);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_GIRAFFE);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_HIPPO);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_LION);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_LIZARD);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_RATTLESNAKE);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_RHINO);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_ROBIN);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_SNAIL);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_SNAKE);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_SPARROW);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_TORTOISE);
        emptyBlacklist(NaturalistTags.Biomes.BLACKLIST_VULTURE);
        coldBlacklist(NaturalistTags.Biomes.BLACKLIST_ZEBRA);
    }

    private void coldBlacklist(TagKey<Biome> tag) {
        tag(tag).addOptionalTag(Tags.Biomes.IS_ICY).addOptionalTag(Tags.Biomes.IS_SNOWY);
    }

    private void emptyBlacklist(TagKey<Biome> tag) {
        tag(tag);
    }
}
