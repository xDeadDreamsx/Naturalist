package com.starfish_studios.naturalist.datagen;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.registry.NaturalistTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class NaturalistEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public NaturalistEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Naturalist.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(NaturalistTags.EntityTypes.ALLIGATOR_HOSTILES)
                .add(NaturalistEntityTypes.DEER.get())
                .add(NaturalistEntityTypes.SNAKE.get())
                .add(EntityType.FROG)
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "shoebill"));

        tag(NaturalistTags.EntityTypes.BEAR_HOSTILES)
                .add(EntityType.SALMON)
                .add(NaturalistEntityTypes.DEER.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "moose"));

        tag(NaturalistTags.EntityTypes.BOAR_HOSTILES)
                .add(NaturalistEntityTypes.SNAKE.get())
                .add(NaturalistEntityTypes.SNAIL.get());

        tag(NaturalistTags.EntityTypes.CATFISH_HOSTILES)
                .add(EntityType.TROPICAL_FISH)
                .add(EntityType.COD)
                .add(EntityType.TADPOLE)
                .add(NaturalistEntityTypes.BASS.get());

        tag(NaturalistTags.EntityTypes.DEER_PREDATORS)
                .add(NaturalistEntityTypes.BEAR.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "grizzly_bear"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "crocodile"));

        tag(NaturalistTags.EntityTypes.LION_HOSTILES)
                .add(NaturalistEntityTypes.RHINO.get())
                .add(NaturalistEntityTypes.ZEBRA.get())
                .add(NaturalistEntityTypes.BOAR.get())
                .add(EntityType.HORSE);

        tag(NaturalistTags.EntityTypes.NATURALIST_ENTITIES)
                .add(NaturalistEntityTypes.ALLIGATOR.get())
                .add(NaturalistEntityTypes.BASS.get())
                .add(NaturalistEntityTypes.BEAR.get())
                .add(NaturalistEntityTypes.BLUEJAY.get())
                .add(NaturalistEntityTypes.BOAR.get())
                .add(NaturalistEntityTypes.BUTTERFLY.get())
                .add(NaturalistEntityTypes.CANARY.get())
                .add(NaturalistEntityTypes.CARDINAL.get())
                .add(NaturalistEntityTypes.CATERPILLAR.get())
                .add(NaturalistEntityTypes.CATFISH.get())
                .add(NaturalistEntityTypes.CORAL_SNAKE.get())
                .add(NaturalistEntityTypes.DEER.get())
                .add(NaturalistEntityTypes.DRAGONFLY.get())
                .add(NaturalistEntityTypes.DUCK.get())
                .add(NaturalistEntityTypes.ELEPHANT.get())
                .add(NaturalistEntityTypes.FIREFLY.get())
                .add(NaturalistEntityTypes.FINCH.get())
                .add(NaturalistEntityTypes.GIRAFFE.get())
                .add(NaturalistEntityTypes.HIPPO.get())
                .add(NaturalistEntityTypes.LION.get())
                .add(NaturalistEntityTypes.LIZARD.get())
                .add(NaturalistEntityTypes.RATTLESNAKE.get())
                .add(NaturalistEntityTypes.RHINO.get())
                .add(NaturalistEntityTypes.ROBIN.get())
                .add(NaturalistEntityTypes.SNAIL.get())
                .add(NaturalistEntityTypes.SNAKE.get())
                .add(NaturalistEntityTypes.SPARROW.get())
                .add(NaturalistEntityTypes.TORTOISE.get())
                .add(NaturalistEntityTypes.VULTURE.get())
                .add(NaturalistEntityTypes.ZEBRA.get());

        tag(NaturalistTags.EntityTypes.SAFE_EGG_WALKERS)
                .add(NaturalistEntityTypes.BUTTERFLY.get())
                .add(NaturalistEntityTypes.CARDINAL.get())
                .add(NaturalistEntityTypes.BLUEJAY.get())
                .add(NaturalistEntityTypes.SPARROW.get())
                .add(NaturalistEntityTypes.FINCH.get())
                .add(NaturalistEntityTypes.ROBIN.get())
                .add(NaturalistEntityTypes.CANARY.get())
                .add(NaturalistEntityTypes.DRAGONFLY.get())
                .add(NaturalistEntityTypes.FIREFLY.get())
                .add(EntityType.BAT)
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "shoebill"));

        tag(NaturalistTags.EntityTypes.SNAKE_HOSTILES)
                .add(EntityType.RABBIT)
                .add(EntityType.CHICKEN)
                .add(EntityType.SILVERFISH)
                .add(NaturalistEntityTypes.SNAIL.get());

        tag(NaturalistTags.EntityTypes.VULTURE_HOSTILES)
                .add(EntityType.ZOMBIE)
                .add(EntityType.ZOMBIE_VILLAGER)
                .add(EntityType.HUSK)
                .add(EntityType.ZOMBIFIED_PIGLIN)
                .add(EntityType.ZOGLIN)
                .add(EntityType.ZOMBIE_HORSE);
    }
}
