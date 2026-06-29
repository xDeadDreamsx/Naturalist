package com.crispytwig.naturalist;

import com.mojang.logging.LogUtils;
import com.crispytwig.naturalist.registry.*;
import com.crispytwig.naturalist.server.entity.base.NaturalistAnimal;
import com.crispytwig.naturalist.server.entity.mob.*;
import com.crispytwig.naturalist.server.item.CaughtMobItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Supplier;

public final class Naturalist {
    public static final String MOD_ID = "naturalist";
    public static final Logger LOGGER = LogUtils.getLogger();

    private Naturalist() {
    }

    public static void bootstrap() {
        touch(NaturalistSoundEvents.SOUND_EVENTS);
        touch(NaturalistEntityTypes.ENTITY_TYPES);
        NaturalistRegistry.init();
        touch(NaturalistMenus.MENUS);
        touch(NaturalistPotions.POTIONS);
        touch(NaturalistRecipes.RECIPE_TYPES);
        touch(NaturalistRecipes.RECIPE_SERIALIZERS);
        touch(NaturalistCreativeTab.CREATIVE_MODE_TABS);
    }

    private static void touch(Object registry) {
    }

    @FunctionalInterface
    public interface AttributeRegistrar {
        void register(EntityType<? extends LivingEntity> type, AttributeSupplier.Builder builder);
    }

    @FunctionalInterface
    public interface SpawnPlacementRegistrar {
        <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate);
    }

    @FunctionalInterface
    public interface BrewingRegistrar {
        void addMix(Holder<Potion> input, Item ingredient, Holder<Potion> output);
    }

    public static void createAttributes(AttributeRegistrar r) {
        r.register(NaturalistEntityTypes.SNAIL.get(), Snail.createAttributes());
        r.register(NaturalistEntityTypes.BEAR.get(), Bear.createAttributes());
        r.register(NaturalistEntityTypes.BUTTERFLY.get(), Butterfly.createAttributes());
        r.register(NaturalistEntityTypes.FIREFLY.get(), Firefly.createAttributes());
        r.register(NaturalistEntityTypes.SNAKE.get(), Snake.createAttributes());
        r.register(NaturalistEntityTypes.CORAL_SNAKE.get(), Snake.createAttributes());
        r.register(NaturalistEntityTypes.RATTLESNAKE.get(), Snake.createAttributes());
        r.register(NaturalistEntityTypes.CRAB.get(), Crab.createAttributes());
        r.register(NaturalistEntityTypes.DEER.get(), Deer.createAttributes());
        r.register(NaturalistEntityTypes.BLUEJAY.get(), Bird.createAttributes());
        r.register(NaturalistEntityTypes.CANARY.get(), Bird.createAttributes());
        r.register(NaturalistEntityTypes.CARDINAL.get(), Bird.createAttributes());
        r.register(NaturalistEntityTypes.ROBIN.get(), Bird.createAttributes());
        r.register(NaturalistEntityTypes.FINCH.get(), Bird.createAttributes());
        r.register(NaturalistEntityTypes.SPARROW.get(), Bird.createAttributes());
        r.register(NaturalistEntityTypes.CATERPILLAR.get(), Caterpillar.createAttributes());
        r.register(NaturalistEntityTypes.RHINO.get(), Rhino.createAttributes());
        r.register(NaturalistEntityTypes.LION.get(), Lion.createAttributes());
        r.register(NaturalistEntityTypes.ELEPHANT.get(), Elephant.createAttributes());
        r.register(NaturalistEntityTypes.ZEBRA.get(), Zebra.createBaseHorseAttributes());
        r.register(NaturalistEntityTypes.GIRAFFE.get(), Giraffe.createAttributes());
        r.register(NaturalistEntityTypes.HIPPO.get(), Hippo.createAttributes());
        r.register(NaturalistEntityTypes.VULTURE.get(), Vulture.createAttributes());
        r.register(NaturalistEntityTypes.BOAR.get(), Boar.createAttributes());
        r.register(NaturalistEntityTypes.DRAGONFLY.get(), Dragonfly.createAttributes());
        r.register(NaturalistEntityTypes.CATFISH.get(), Catfish.createAttributes());
        r.register(NaturalistEntityTypes.ALLIGATOR.get(), Alligator.createAttributes());
        r.register(NaturalistEntityTypes.BASS.get(), Mob.createMobAttributes());
        r.register(NaturalistEntityTypes.LIZARD.get(), Lizard.createAttributes());
        r.register(NaturalistEntityTypes.LIZARD_TAIL.get(), LizardTail.createAttributes());
        r.register(NaturalistEntityTypes.TORTOISE.get(), Tortoise.createAttributes());
        r.register(NaturalistEntityTypes.DUCK.get(), Duck.createAttributes());
        r.register(NaturalistEntityTypes.STARFISH.get(), Starfish.createAttributes());
    }

    public static void registerSpawnPlacements(SpawnPlacementRegistrar r) {
        r.register(NaturalistEntityTypes.SNAIL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.BEAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Butterfly::checkButterflySpawnRules);
        r.register(NaturalistEntityTypes.FIREFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Firefly::checkFireflySpawnRules);
        r.register(NaturalistEntityTypes.SNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules);
        r.register(NaturalistEntityTypes.CORAL_SNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules);
        r.register(NaturalistEntityTypes.RATTLESNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules);
        r.register(NaturalistEntityTypes.CRAB.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Crab::checkCrabSpawnRules);
        r.register(NaturalistEntityTypes.DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.BLUEJAY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules);
        r.register(NaturalistEntityTypes.CANARY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules);
        r.register(NaturalistEntityTypes.CARDINAL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules);
        r.register(NaturalistEntityTypes.ROBIN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules);
        r.register(NaturalistEntityTypes.FINCH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules);
        r.register(NaturalistEntityTypes.SPARROW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules);
        r.register(NaturalistEntityTypes.RHINO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.LION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.ELEPHANT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.ZEBRA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.GIRAFFE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.HIPPO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Hippo::checkHippoSpawnRules);
        r.register(NaturalistEntityTypes.VULTURE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Vulture::checkVultureSpawnRules);
        r.register(NaturalistEntityTypes.BOAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.DRAGONFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Dragonfly::checkDragonflySpawnRules);
        r.register(NaturalistEntityTypes.CATFISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        r.register(NaturalistEntityTypes.ALLIGATOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Alligator::checkAlligatorSpawnRules);
        r.register(NaturalistEntityTypes.BASS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules);
        r.register(NaturalistEntityTypes.LIZARD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.TORTOISE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules);
        r.register(NaturalistEntityTypes.DUCK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Duck::checkDuckSpawnRules);
        r.register(NaturalistEntityTypes.STARFISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Starfish::checkStarfishSpawnRules);
    }

    public static void registerPotionMixes(BrewingRegistrar r) {
        r.addMix(Potions.AWKWARD, NaturalistRegistry.ANTLER.get(), potion(NaturalistPotions.FOREST_DASHER));
        r.addMix(potion(NaturalistPotions.FOREST_DASHER), Items.REDSTONE, potion(NaturalistPotions.LONG_FOREST_DASHER));
        r.addMix(potion(NaturalistPotions.FOREST_DASHER), Items.GLOWSTONE_DUST, potion(NaturalistPotions.STRONG_FOREST_DASHER));
    }

    private static Holder<Potion> potion(Supplier<Potion> potion) {
        return BuiltInRegistries.POTION.wrapAsHolder(potion.get());
    }

    public static void registerDispenserBehaviors() {
        DispenserBlock.registerBehavior(NaturalistRegistry.DUCK_EGG.get(), new ProjectileDispenseBehavior(NaturalistRegistry.DUCK_EGG.get()));

        DispenseItemBehavior bucketDispenseBehavior = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            public @NotNull ItemStack execute(@NotNull BlockSource source, ItemStack stack) {
                BlockPos blockPos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                Level level = source.level();
                if (stack.getItem() instanceof DispensibleContainerItem dispensibleContainerItem) {
                    if (dispensibleContainerItem.emptyContents(null, level, blockPos, null)) {
                        dispensibleContainerItem.checkExtraContent(null, level, stack, blockPos);
                        return new ItemStack(Items.BUCKET);
                    }
                }
                return this.defaultDispenseItemBehavior.dispense(source, stack);
            }
        };
        DispenserBlock.registerBehavior(NaturalistRegistry.BASS_BUCKET.get(), bucketDispenseBehavior);
        DispenserBlock.registerBehavior(NaturalistRegistry.CATFISH_BUCKET.get(), bucketDispenseBehavior);
        DispenserBlock.registerBehavior(NaturalistRegistry.STARFISH_BUCKET.get(), bucketDispenseBehavior);

        DispenserBlock.registerBehavior(NaturalistRegistry.SNAIL_BUCKET.get(), new DefaultDispenseItemBehavior() {
            public @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                BlockPos blockPos = source.pos().relative(direction);
                ServerLevel serverLevel = source.level();

                EntityType<Snail> entityType = NaturalistEntityTypes.SNAIL.get();
                Snail snail = entityType.spawn(serverLevel, blockPos, MobSpawnType.DISPENSER);
                if (snail != null) {
                    stack.shrink(1);
                    return new ItemStack(Items.BUCKET);
                }
                return stack;
            }
        });

        DispenserBlock.registerBehavior(NaturalistRegistry.BUTTERFLY.get(), new DefaultDispenseItemBehavior() {
            public @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                BlockPos blockPos = source.pos().relative(direction);
                ServerLevel serverLevel = source.level();

                EntityType<Butterfly> entityType = NaturalistEntityTypes.BUTTERFLY.get();
                Butterfly butterfly = entityType.spawn(serverLevel, blockPos, MobSpawnType.DISPENSER);
                if (butterfly != null) {
                    stack.shrink(1);
                }
                return stack;
            }
        });

        DispenserBlock.registerBehavior(NaturalistRegistry.CRAB.get(), new DefaultDispenseItemBehavior() {
            public @NotNull ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
                Direction direction = source.state().getValue(DispenserBlock.FACING);
                BlockPos blockPos = source.pos().relative(direction);
                Level level = source.level();
                if (stack.getItem() instanceof CaughtMobItem caughtMobItem) {
                    caughtMobItem.checkExtraContent(null, level, stack, blockPos);
                    stack.shrink(1);
                }
                return stack;
            }
        });
    }
}
