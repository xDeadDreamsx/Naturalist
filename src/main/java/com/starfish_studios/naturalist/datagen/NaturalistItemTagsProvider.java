package com.starfish_studios.naturalist.datagen;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.registry.NaturalistRegistry;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NaturalistItemTagsProvider extends ItemTagsProvider {
    public NaturalistItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, Naturalist.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(NaturalistTags.ItemTags.ALLIGATOR_FOOD_ITEMS)
                .add(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.RABBIT);

        tag(NaturalistTags.ItemTags.BEAR_FURS)
                .add(NaturalistRegistry.FUR.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "bear_fur"));

        tag(NaturalistTags.ItemTags.BEAR_TEMPT_ITEMS)
                .add(Items.SALMON, Items.COOKED_SALMON, Items.HONEYCOMB, Items.SWEET_BERRIES)
                .add(NaturalistRegistry.VENISON.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "raw_moose_ribs"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "cooked_moose_ribs"));

        tag(NaturalistTags.ItemTags.BIRD_FOOD_ITEMS)
                .add(Items.WHEAT_SEEDS, Items.PUMPKIN_SEEDS, Items.MELON_SEEDS, Items.BEETROOT_SEEDS);

        tag(NaturalistTags.ItemTags.BOAR_FOOD_ITEMS)
                .add(Items.CARROT, Items.POTATO, Items.BEETROOT);

        tag(NaturalistTags.ItemTags.CRAB_FOOD)
                .add(Items.TROPICAL_FISH);

        tag(NaturalistTags.ItemTags.DUCK_FOOD_ITEMS)
                .addTag(Tags.Items.FOODS_RAW_FISH);

        tag(NaturalistTags.ItemTags.EGGS)
                .add(NaturalistRegistry.TORTOISE_EGG.get().asItem())
                .add(NaturalistRegistry.ALLIGATOR_EGG.get().asItem())
                .add(NaturalistRegistry.DUCK_EGG.get())
                .add(Items.EGG);

        tag(NaturalistTags.ItemTags.GIRAFFE_FOOD_ITEMS)
                .add(Items.APPLE, Items.GOLDEN_APPLE, Items.HAY_BLOCK);

        tag(NaturalistTags.ItemTags.LIZARD_TEMPT_ITEMS)
                .add(Items.SPIDER_EYE);

        tag(NaturalistTags.ItemTags.SHEARS)
                .add(Items.SHEARS)
                .addOptionalTag(Tags.Items.TOOLS_SHEAR);

        tag(NaturalistTags.ItemTags.SNAKE_TEMPT_ITEMS)
                .add(Items.CHICKEN, Items.RABBIT, Items.RABBIT_FOOT, Items.EGG);

        tag(NaturalistTags.ItemTags.TORTOISE_TEMPT_ITEMS)
                .add(Items.BAMBOO, Items.DANDELION, Items.BROWN_MUSHROOM, Items.CACTUS);
    }
}
