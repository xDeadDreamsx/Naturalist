package com.starfish_studios.naturalist;

import com.mojang.logging.LogUtils;
import com.starfish_studios.naturalist.datagen.NaturalistDataGenerators;
import com.starfish_studios.naturalist.server.entity.base.NaturalistAnimal;
import com.starfish_studios.naturalist.server.entity.mob.*;
import com.starfish_studios.naturalist.registry.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.DispensibleContainerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Optional;

@Mod(Naturalist.MOD_ID)
public class Naturalist {
    public static final String MOD_ID = "naturalist";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Naturalist(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NaturalistConfig.COMMON_CONFIG_SPEC, "naturalist-server.toml");

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::createAttributes);
        modEventBus.addListener(this::registerSpawnPlacements);
        modEventBus.addListener(this::addPackFinders);
        modEventBus.addListener(NaturalistDataGenerators::gatherData);

        NeoForge.EVENT_BUS.addListener(this::registerBrewingRecipes);
        NeoForge.EVENT_BUS.addListener(Naturalist::onFinalizeSpawn);
        NeoForge.EVENT_BUS.addListener(Naturalist::onMobEffectApplicable);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
            modEventBus.addListener(NaturalistClient::registerEntityRenderers);
            modEventBus.addListener(NaturalistClient::registerLayerDefinitions);
            modEventBus.addListener(NaturalistClient::registerItemProperties);
        }

        NaturalistRegistry.BLOCKS.register(modEventBus);
        NaturalistRegistry.ITEMS.register(modEventBus);
        NaturalistEntityTypes.ENTITY_TYPES.register(modEventBus);
        NaturalistSoundEvents.SOUND_EVENTS.register(modEventBus);
        NaturalistPotions.POTIONS.register(modEventBus);
        NaturalistRecipes.RECIPE_TYPES.register(modEventBus);
        NaturalistRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        NaturalistBiomeModifiers.BIOME_MODIFIERS.register(modEventBus);
        NaturalistCreativeTab.CREATIVE_MODE_TABS.register(modEventBus);
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.CLIENT_RESOURCES) {
            Path resourcePath = ModList.get().getModFileById(MOD_ID).getFile().findResource("resourcepacks/custom_spawn_eggs");
            event.addRepositorySource(consumer -> {
                PackLocationInfo info = new PackLocationInfo(
                        "naturalist:custom_spawn_eggs",
                        Component.literal("Naturalist 1.21.5+ Spawn Eggs"),
                        PackSource.BUILT_IN,
                        Optional.empty()
                );
                Pack pack = Pack.readMetaAndCreate(
                        info,
                        new PathPackResources.PathResourcesSupplier(resourcePath),
                        PackType.CLIENT_RESOURCES,
                        new PackSelectionConfig(false, Pack.Position.TOP, false)
                );
                if (pack != null) consumer.accept(pack);
            });
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(Naturalist::registerDispenserBehaviors);
    }

    private void createAttributes(EntityAttributeCreationEvent event) {
        event.put(NaturalistEntityTypes.SNAIL.get(), Snail.createAttributes().build());
        event.put(NaturalistEntityTypes.BEAR.get(), Bear.createAttributes().build());
        event.put(NaturalistEntityTypes.BUTTERFLY.get(), Butterfly.createAttributes().build());
        event.put(NaturalistEntityTypes.FIREFLY.get(), Firefly.createAttributes().build());
        event.put(NaturalistEntityTypes.SNAKE.get(), Snake.createAttributes().build());
        event.put(NaturalistEntityTypes.CORAL_SNAKE.get(), Snake.createAttributes().build());
        event.put(NaturalistEntityTypes.RATTLESNAKE.get(), Snake.createAttributes().build());
        event.put(NaturalistEntityTypes.DEER.get(), Deer.createAttributes().build());
        event.put(NaturalistEntityTypes.BLUEJAY.get(), Bird.createAttributes().build());
        event.put(NaturalistEntityTypes.CANARY.get(), Bird.createAttributes().build());
        event.put(NaturalistEntityTypes.CARDINAL.get(), Bird.createAttributes().build());
        event.put(NaturalistEntityTypes.ROBIN.get(), Bird.createAttributes().build());
        event.put(NaturalistEntityTypes.FINCH.get(), Bird.createAttributes().build());
        event.put(NaturalistEntityTypes.SPARROW.get(), Bird.createAttributes().build());
        event.put(NaturalistEntityTypes.CATERPILLAR.get(), Caterpillar.createAttributes().build());
        event.put(NaturalistEntityTypes.RHINO.get(), Rhino.createAttributes().build());
        event.put(NaturalistEntityTypes.LION.get(), Lion.createAttributes().build());
        event.put(NaturalistEntityTypes.ELEPHANT.get(), Elephant.createAttributes().build());
        event.put(NaturalistEntityTypes.ZEBRA.get(), Zebra.createBaseHorseAttributes().build());
        event.put(NaturalistEntityTypes.GIRAFFE.get(), Giraffe.createAttributes().build());
        event.put(NaturalistEntityTypes.HIPPO.get(), Hippo.createAttributes().build());
        event.put(NaturalistEntityTypes.VULTURE.get(), Vulture.createAttributes().build());
        event.put(NaturalistEntityTypes.BOAR.get(), Boar.createAttributes().build());
        event.put(NaturalistEntityTypes.DRAGONFLY.get(), Dragonfly.createAttributes().build());
        event.put(NaturalistEntityTypes.CATFISH.get(), Catfish.createAttributes().build());
        event.put(NaturalistEntityTypes.ALLIGATOR.get(), Alligator.createAttributes().build());
        event.put(NaturalistEntityTypes.BASS.get(), Mob.createMobAttributes().build());
        event.put(NaturalistEntityTypes.LIZARD.get(), Lizard.createAttributes().build());
        event.put(NaturalistEntityTypes.LIZARD_TAIL.get(), LizardTail.createAttributes().build());
        event.put(NaturalistEntityTypes.TORTOISE.get(), Tortoise.createAttributes().build());
        event.put(NaturalistEntityTypes.DUCK.get(), Duck.createAttributes().build());
    }

    private void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(NaturalistEntityTypes.SNAIL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.BEAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.BUTTERFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Butterfly::checkButterflySpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.FIREFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Firefly::checkFireflySpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.SNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.CORAL_SNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.RATTLESNAKE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Snake::checkSnakeSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.DEER.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        event.register(NaturalistEntityTypes.BLUEJAY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.CANARY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.CARDINAL.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.ROBIN.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.FINCH.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.SPARROW.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Bird::checkBirdSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        event.register(NaturalistEntityTypes.RHINO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.LION.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.ELEPHANT.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.ZEBRA.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.GIRAFFE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.HIPPO.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Hippo::checkHippoSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.VULTURE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Vulture::checkVultureSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.BOAR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);

        event.register(NaturalistEntityTypes.DRAGONFLY.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING, Dragonfly::checkDragonflySpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.CATFISH.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.ALLIGATOR.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Alligator::checkAlligatorSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.BASS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.LIZARD.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.TORTOISE.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, NaturalistAnimal::checkNaturalistAnimalSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
        event.register(NaturalistEntityTypes.DUCK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Duck::checkDuckSpawnRules, RegisterSpawnPlacementsEvent.Operation.AND);
    }

    private void registerBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, NaturalistRegistry.ANTLER.get(), NaturalistPotions.FOREST_DASHER);
        builder.addMix(NaturalistPotions.FOREST_DASHER, Items.REDSTONE, NaturalistPotions.LONG_FOREST_DASHER);
        builder.addMix(NaturalistPotions.FOREST_DASHER, Items.GLOWSTONE_DUST, NaturalistPotions.STRONG_FOREST_DASHER);
    }

    private static void registerDispenserBehaviors() {
        DispenserBlock.registerBehavior(NaturalistRegistry.DUCK_EGG.get(), new ProjectileDispenseBehavior(NaturalistRegistry.DUCK_EGG.get()));

        DispenseItemBehavior bucketDispenseBehavior = new DefaultDispenseItemBehavior() {
            private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();

            public @NotNull ItemStack execute(@NotNull BlockSource source, ItemStack stack) {
                BlockPos blockPos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
                Level level = source.level();
                if (stack.getItem() instanceof DispensibleContainerItem dispensibleContainerItem) {
                    if (dispensibleContainerItem.emptyContents(null, level, blockPos, null, stack)) {
                        dispensibleContainerItem.checkExtraContent(null, level, stack, blockPos);
                        return new ItemStack(Items.BUCKET);
                    }
                }
                return this.defaultDispenseItemBehavior.dispense(source, stack);
            }
        };
        DispenserBlock.registerBehavior(NaturalistRegistry.BASS_BUCKET.get(), bucketDispenseBehavior);
        DispenserBlock.registerBehavior(NaturalistRegistry.CATFISH_BUCKET.get(), bucketDispenseBehavior);

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
    }

    private static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (event.getEntity() instanceof Dragonfly dragonfly) {
            dragonfly.initVariantOnSpawn(event.getLevel());
        }
    }

    private static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Vulture && event.getEffectInstance().is(MobEffects.HUNGER)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }
}
