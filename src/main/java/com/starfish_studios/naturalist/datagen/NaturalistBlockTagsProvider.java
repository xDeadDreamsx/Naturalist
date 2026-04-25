package com.starfish_studios.naturalist.datagen;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.registry.NaturalistRegistry;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NaturalistBlockTagsProvider extends BlockTagsProvider {
    public NaturalistBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Naturalist.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(NaturalistTags.BlockTags.FIREFLIES_SPAWNABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.MUD)
                .addTag(BlockTags.LEAVES);

        tag(NaturalistTags.BlockTags.DRAGONFLIES_SPAWNABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.MUD)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.SHORT_GRASS);

        tag(NaturalistTags.BlockTags.BUTTERFLIES_SPAWNABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.MUD)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.SHORT_GRASS);

        tag(NaturalistTags.BlockTags.VULTURES_SPAWNABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.AIR)
                .addTag(BlockTags.LEAVES)
                .addTag(BlockTags.SAND);

        tag(NaturalistTags.BlockTags.DUCKS_SPAWNABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.MUD, Blocks.DIRT, Blocks.WATER);

        tag(NaturalistTags.BlockTags.RHINO_CHARGE_BREAKABLE)
                .addTag(BlockTags.CROPS)
                .addTag(BlockTags.FLOWERS)
                .add(Blocks.SHORT_GRASS, Blocks.FERN, Blocks.TALL_GRASS, Blocks.LARGE_FERN);

        tag(NaturalistTags.BlockTags.VULTURE_PERCH_BLOCKS)
                .add(Blocks.CACTUS)
                .addTag(BlockTags.LEAVES);

        tag(NaturalistTags.BlockTags.CATTAIL_PLACEABLE)
                .add(Blocks.MUD, Blocks.DIRT, Blocks.GRASS_BLOCK);

        tag(NaturalistTags.BlockTags.ALLIGATOR_EGG_LAYABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.MUD);

        tag(NaturalistTags.BlockTags.TORTOISE_EGG_LAYABLE_ON)
                .add(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND, Blocks.MUD);

        tag(NaturalistTags.BlockTags.MOTHS_ATTRACTED_TO)
                .add(Blocks.TORCH, Blocks.GLOWSTONE, Blocks.SEA_LANTERN, Blocks.JACK_O_LANTERN,
                        Blocks.BEACON, Blocks.END_ROD, Blocks.SHROOMLIGHT, Blocks.CAMPFIRE,
                        Blocks.SOUL_CAMPFIRE, Blocks.LANTERN, Blocks.SOUL_LANTERN);

        tag(NaturalistTags.BlockTags.SHELLSTONE)
                .add(NaturalistRegistry.SHELLSTONE.get())
                .add(NaturalistRegistry.SHELLSTONE_STAIRS.get())
                .add(NaturalistRegistry.SHELLSTONE_SLAB.get())
                .add(NaturalistRegistry.SHELLSTONE_WALL.get())
                .add(NaturalistRegistry.SHELLSTONE_BRICKS.get())
                .add(NaturalistRegistry.SHELLSTONE_BRICK_STAIRS.get())
                .add(NaturalistRegistry.SHELLSTONE_BRICK_SLAB.get())
                .add(NaturalistRegistry.SHELLSTONE_BRICK_WALL.get())
                .add(NaturalistRegistry.CUT_SHELLSTONE.get())
                .add(NaturalistRegistry.CUT_SHELLSTONE_STAIRS.get())
                .add(NaturalistRegistry.CUT_SHELLSTONE_SLAB.get())
                .add(NaturalistRegistry.CUT_SHELLSTONE_WALL.get())
                .add(NaturalistRegistry.SMOOTH_SHELLSTONE.get())
                .add(NaturalistRegistry.SMOOTH_SHELLSTONE_STAIRS.get())
                .add(NaturalistRegistry.SMOOTH_SHELLSTONE_SLAB.get())
                .add(NaturalistRegistry.SMOOTH_SHELLSTONE_WALL.get());
    }
}
