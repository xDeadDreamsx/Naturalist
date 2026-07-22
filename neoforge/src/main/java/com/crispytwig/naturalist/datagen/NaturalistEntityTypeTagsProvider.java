package com.crispytwig.naturalist.datagen;

import com.crispytwig.naturalist.Naturalist;
import com.crispytwig.naturalist.registry.NaturalistEntityTypes;
import com.crispytwig.naturalist.registry.NaturalistTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class NaturalistEntityTypeTagsProvider extends EntityTypeTagsProvider {
    public NaturalistEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, Naturalist.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider provider) {
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

        tag(NaturalistTags.EntityTypes.ANGLERFISH_HOSTILES)
                .add(EntityType.TROPICAL_FISH)
                .add(EntityType.COD);

        tag(NaturalistTags.EntityTypes.DEER_PREDATORS)
                .add(NaturalistEntityTypes.BEAR.get())
                .add(NaturalistEntityTypes.BLACK_BEAR.get())
                .add(NaturalistEntityTypes.TIGER.get())
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "grizzly_bear"))
                .addOptional(ResourceLocation.fromNamespaceAndPath("alexsmobs", "crocodile"));

        tag(NaturalistTags.EntityTypes.LION_HOSTILES)
                .add(NaturalistEntityTypes.RHINO.get())
                .add(NaturalistEntityTypes.ZEBRA.get())
                .add(NaturalistEntityTypes.BOAR.get())
                .add(EntityType.HORSE);

        tag(NaturalistTags.EntityTypes.TIGER_HOSTILES)
                .add(NaturalistEntityTypes.BOAR.get())
                .add(EntityType.PIG)
                .add(NaturalistEntityTypes.DEER.get())
                .add(NaturalistEntityTypes.ZEBRA.get())
                .add(NaturalistEntityTypes.SNAKE.get());

        tag(NaturalistTags.EntityTypes.KOMODO_DRAGON_HOSTILES)
                .add(EntityType.CHICKEN)
                .add(EntityType.RABBIT)
                .add(NaturalistEntityTypes.LIZARD.get())
                .add(NaturalistEntityTypes.SNAKE.get())
                .add(NaturalistEntityTypes.BOAR.get());

        tag(NaturalistTags.EntityTypes.SCORPION_HOSTILES)
                .add(NaturalistEntityTypes.LIZARD.get());

        tag(NaturalistTags.EntityTypes.GREAT_WHITE_SHARK_HOSTILES)
                .add(EntityType.COD)
                .add(EntityType.SALMON)
                .add(EntityType.TROPICAL_FISH)
                .add(NaturalistEntityTypes.BASS.get())
                .add(NaturalistEntityTypes.CATFISH.get())
                .add(NaturalistEntityTypes.PIRANHA.get())
                .add(NaturalistEntityTypes.BLOBFISH.get())
                .add(NaturalistEntityTypes.ANGLERFISH.get());

        tag(NaturalistTags.EntityTypes.TURKEY_HOSTILES)
                .add(NaturalistEntityTypes.ANT.get());

        tag(NaturalistTags.EntityTypes.NATURALIST_ENTITIES)
                .add(NaturalistEntityTypes.ALLIGATOR.get())
                .add(NaturalistEntityTypes.BASS.get())
                .add(NaturalistEntityTypes.BEAR.get())
                .add(NaturalistEntityTypes.BIRD.get())
                .add(NaturalistEntityTypes.BOAR.get())
                .add(NaturalistEntityTypes.BUTTERFLY.get())
                .add(NaturalistEntityTypes.CAPYBARA.get())
                .add(NaturalistEntityTypes.CATERPILLAR.get())
                .add(NaturalistEntityTypes.CATFISH.get())
                .add(NaturalistEntityTypes.DEER.get())
                .add(NaturalistEntityTypes.DESERT_SCORPION.get())
                .add(NaturalistEntityTypes.DRAGONFLY.get())
                .add(NaturalistEntityTypes.DUCK.get())
                .add(NaturalistEntityTypes.ELEPHANT.get())
                .add(NaturalistEntityTypes.FIREFLY.get())
                .add(NaturalistEntityTypes.GIRAFFE.get())
                .add(NaturalistEntityTypes.GREAT_WHITE_SHARK.get())
                .add(NaturalistEntityTypes.HIPPO.get())
                .add(NaturalistEntityTypes.JUNGLE_SCORPION.get())
                .add(NaturalistEntityTypes.LION.get())
                .add(NaturalistEntityTypes.LIZARD.get())
                .add(NaturalistEntityTypes.MAMMOTH.get())
                .add(NaturalistEntityTypes.RHINO.get())
                .add(NaturalistEntityTypes.SNAIL.get())
                .add(NaturalistEntityTypes.SNAKE.get())
                .add(NaturalistEntityTypes.TORTOISE.get())
                .add(NaturalistEntityTypes.TURKEY.get())
                .add(NaturalistEntityTypes.VULTURE.get())
                .add(NaturalistEntityTypes.ZEBRA.get());

        tag(NaturalistTags.EntityTypes.SAFE_EGG_WALKERS)
                .add(NaturalistEntityTypes.BUTTERFLY.get())
                .add(NaturalistEntityTypes.BIRD.get())
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
