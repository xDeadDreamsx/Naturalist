package com.starfish_studios.naturalist.registry;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.entity.mob.*;
import com.starfish_studios.naturalist.server.entity.misc.ThrownDuckEgg;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NaturalistEntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Naturalist.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<Alligator>> ALLIGATOR = register("alligator", EntityType.Builder.of(Alligator::new, MobCategory.CREATURE).sized(1.8F, 0.8F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Bass>> BASS = register("bass", EntityType.Builder.of(Bass::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.4F).clientTrackingRange(4));
    public static final DeferredHolder<EntityType<?>, EntityType<Bear>> BEAR = register("bear", EntityType.Builder.of(Bear::new, MobCategory.CREATURE).sized(1.4F, 1.7F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Bird>> BLUEJAY = register("bluejay", EntityType.Builder.of(Bird::new, MobCategory.CREATURE).sized(0.5F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Boar>> BOAR = register("boar", EntityType.Builder.of(Boar::new, MobCategory.CREATURE).sized(0.9F, 0.9F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Butterfly>> BUTTERFLY = register("butterfly", EntityType.Builder.of(Butterfly::new, MobCategory.AMBIENT).sized(0.7F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Bird>> CANARY = register("canary", EntityType.Builder.of(Bird::new, MobCategory.CREATURE).sized(0.5F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Bird>> CARDINAL = register("cardinal", EntityType.Builder.of(Bird::new, MobCategory.CREATURE).sized(0.5F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Caterpillar>> CATERPILLAR = register("caterpillar", EntityType.Builder.of(Caterpillar::new, MobCategory.CREATURE).sized(0.4F, 0.4F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Catfish>> CATFISH = register("catfish", EntityType.Builder.of(Catfish::new, MobCategory.WATER_AMBIENT).sized(0.7F, 0.4F).clientTrackingRange(4));
    public static final DeferredHolder<EntityType<?>, EntityType<Snake>> CORAL_SNAKE = register("coral_snake", EntityType.Builder.of(Snake::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Deer>> DEER = register("deer", EntityType.Builder.of(Deer::new, MobCategory.CREATURE).sized(1.3F, 1.6F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Dragonfly>> DRAGONFLY = register("dragonfly", EntityType.Builder.of(Dragonfly::new, MobCategory.AMBIENT).sized(0.9F, 0.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Duck>> DUCK = register("duck", EntityType.Builder.of(Duck::new, MobCategory.CREATURE).sized(0.6F, 1.0F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownDuckEgg>> DUCK_EGG = register("duck_egg", EntityType.Builder.<ThrownDuckEgg>of(ThrownDuckEgg::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(16));
    public static final DeferredHolder<EntityType<?>, EntityType<Elephant>> ELEPHANT = register("elephant", EntityType.Builder.of(Elephant::new, MobCategory.CREATURE).sized(2.5F, 3.5F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Firefly>> FIREFLY = register("firefly", EntityType.Builder.of(Firefly::new, MobCategory.AMBIENT).sized(0.7F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Bird>> FINCH = register("finch", EntityType.Builder.of(Bird::new, MobCategory.CREATURE).sized(0.5F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Giraffe>> GIRAFFE = register("giraffe", EntityType.Builder.of(Giraffe::new, MobCategory.CREATURE).sized(1.9F, 5.4F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Hippo>> HIPPO = register("hippo", EntityType.Builder.of(Hippo::new, MobCategory.CREATURE).sized(1.8F, 1.8F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Lion>> LION = register("lion", EntityType.Builder.of(Lion::new, MobCategory.CREATURE).sized(1.5F, 1.8F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Lizard>> LIZARD = register("lizard", EntityType.Builder.of(Lizard::new, MobCategory.CREATURE).sized(0.8F, 0.5F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<LizardTail>> LIZARD_TAIL = register("lizard_tail", EntityType.Builder.of(LizardTail::new, MobCategory.CREATURE).sized(0.7F, 0.5F).clientTrackingRange(10));

    public static final DeferredHolder<EntityType<?>, EntityType<Snake>> RATTLESNAKE = register("rattlesnake", EntityType.Builder.of(Snake::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Rhino>> RHINO = register("rhino", EntityType.Builder.of(Rhino::new, MobCategory.CREATURE).sized(2.5F, 3.0F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Bird>> ROBIN = register("robin", EntityType.Builder.of(Bird::new, MobCategory.CREATURE).sized(0.5F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Snail>> SNAIL = register("snail", EntityType.Builder.of(Snail::new, MobCategory.CREATURE).sized(0.7F, 0.7F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Snake>> SNAKE = register("snake", EntityType.Builder.of(Snake::new, MobCategory.CREATURE).sized(0.6F, 0.7F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Bird>> SPARROW = register("sparrow", EntityType.Builder.of(Bird::new, MobCategory.CREATURE).sized(0.5F, 0.6F).clientTrackingRange(8));
    public static final DeferredHolder<EntityType<?>, EntityType<Tortoise>> TORTOISE = register("tortoise", EntityType.Builder.of(Tortoise::new, MobCategory.CREATURE).sized(1.2F, 0.875F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Vulture>> VULTURE = register("vulture", EntityType.Builder.of(Vulture::new, MobCategory.CREATURE).sized(0.9F, 0.5F).clientTrackingRange(10));
    public static final DeferredHolder<EntityType<?>, EntityType<Zebra>> ZEBRA = register("zebra", EntityType.Builder.of(Zebra::new, MobCategory.CREATURE).sized(1.3964844F, 1.5F).clientTrackingRange(10));

    private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String id, EntityType.Builder<T> builder) {
        return ENTITY_TYPES.register(id, () -> builder.build(ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, id).toString()));
    }
}
