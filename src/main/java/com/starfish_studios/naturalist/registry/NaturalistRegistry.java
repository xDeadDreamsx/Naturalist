package com.starfish_studios.naturalist.registry;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.server.block.*;
import com.starfish_studios.naturalist.server.entity.mob.Butterfly;
import com.starfish_studios.naturalist.server.entity.mob.Snail;
import com.starfish_studios.naturalist.server.item.BugNetItem;
import com.starfish_studios.naturalist.server.item.DuckEggItem;
import com.starfish_studios.naturalist.server.item.GlowGoopItem;
import com.starfish_studios.naturalist.server.item.CaughtMobItem;
import com.starfish_studios.naturalist.server.item.CaughtMobWithVariantsItem;
import com.starfish_studios.naturalist.server.item.NoFluidMobBucketWithVariantsItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public class NaturalistRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Naturalist.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Naturalist.MOD_ID);

    public static final DeferredHolder<Item, Item> BUSHMEAT = ITEMS.register("bushmeat", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.3F).build())));
    public static final DeferredHolder<Item, Item> COOKED_BUSHMEAT = ITEMS.register("cooked_bushmeat", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build())));
    public static final DeferredHolder<Item, Item> FUR = ITEMS.register("fur", () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Block, AlligatorEggBlock> ALLIGATOR_EGG = registerBlock("alligator_egg", () -> new AlligatorEggBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TURTLE_EGG)));
    public static final DeferredHolder<Item, DuckEggItem> DUCK_EGG = ITEMS.register("duck_egg", () -> new DuckEggItem(new Item.Properties()));
    public static final DeferredHolder<Block, TortoiseEggBlock> TORTOISE_EGG = registerBlock("tortoise_egg", () -> new TortoiseEggBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TURTLE_EGG)));
    public static final DeferredHolder<Item, Item> COOKED_EGG = ITEMS.register("cooked_egg", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
    public static final DeferredHolder<Block, SnailEggBlock> SNAIL_EGGS = registerBlock("snail_eggs", () -> new SnailEggBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FROGSPAWN)));

    public static final DeferredHolder<Item, Item> ANTLER = ITEMS.register("antler", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Block, GlowGoopBlock> GLOW_GOOP_BLOCK = registerBlockOnly("glow_goop", () -> new GlowGoopBlock(BlockBehaviour.Properties.of().strength(0.5F).replaceable().noOcclusion().noCollission().lightLevel(GlowGoopBlock.LIGHT_EMISSION).sound(SoundType.HONEY_BLOCK)));
    public static final DeferredHolder<Item, GlowGoopItem> GLOW_GOOP = ITEMS.register("glow_goop", () -> new GlowGoopItem(GLOW_GOOP_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Block, TeddyBearBlock> PLUSH_BEAR = registerBlock("plush_bear", () -> new TeddyBearBlock(BlockBehaviour.Properties.of().strength(0.8f).sound(SoundType.WOOL).noOcclusion()));
    public static final DeferredHolder<Item, Item> DUCK = ITEMS.register("duck", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())));
    public static final DeferredHolder<Item, Item> COOKED_DUCK = ITEMS.register("cooked_duck", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6F).build())));
    public static final DeferredHolder<Item, Item> VENISON = ITEMS.register("venison", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3F).build())));
    public static final DeferredHolder<Item, Item> COOKED_VENISON = ITEMS.register("cooked_venison", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build())));
    public static final DeferredHolder<Item, Item> LIZARD_TAIL = ITEMS.register("lizard_tail", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.8F).effect(() -> new MobEffectInstance(MobEffects.POISON, 100, 0), 1.0F).build())));
    public static final DeferredHolder<Item, Item> COOKED_LIZARD_TAIL = ITEMS.register("cooked_lizard_tail", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
    public static final DeferredHolder<Item, MobBucketItem> CATFISH_BUCKET = ITEMS.register("catfish_bucket", () -> new MobBucketItem(NaturalistEntityTypes.CATFISH.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, MobBucketItem> BASS_BUCKET = ITEMS.register("bass_bucket", () -> new MobBucketItem(NaturalistEntityTypes.BASS.get(), Fluids.WATER, SoundEvents.BUCKET_EMPTY_FISH, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> CATFISH = ITEMS.register("catfish", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())));
    public static final DeferredHolder<Item, Item> COOKED_CATFISH = ITEMS.register("cooked_catfish", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build())));
    public static final DeferredHolder<Item, Item> BASS = ITEMS.register("bass", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1F).build())));
    public static final DeferredHolder<Item, Item> COOKED_BASS = ITEMS.register("cooked_bass", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.6F).build())));
    public static final DeferredHolder<Item, BugNetItem> CAPTURE_NET = ITEMS.register("capture_net", () -> new BugNetItem(new Item.Properties().durability(64)));
    public static final DeferredHolder<Block, ChrysalisBlock> CHRYSALIS_BLOCK = registerBlockOnly("chrysalis", () -> new ChrysalisBlock(BlockBehaviour.Properties.of().randomTicks().strength(0.2F, 3.0F).sound(SoundType.GRASS).noOcclusion().noCollission().pushReaction(PushReaction.DESTROY)));
    public static final DeferredHolder<Item, BlockItem> CHRYSALIS = ITEMS.register("chrysalis", () -> new BlockItem(CHRYSALIS_BLOCK.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, CaughtMobItem> CATERPILLAR = ITEMS.register("caterpillar", () -> new CaughtMobItem(NaturalistEntityTypes.CATERPILLAR, () -> Fluids.EMPTY, NaturalistSoundEvents.SNAIL_FORWARD, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, CaughtMobWithVariantsItem> BUTTERFLY = ITEMS.register("butterfly", () -> new CaughtMobWithVariantsItem(NaturalistEntityTypes.BUTTERFLY, () -> Fluids.EMPTY, NaturalistSoundEvents.BIRD_FLY, Butterfly.Variant.values().length, new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> SNAIL_SHELL = ITEMS.register("snail_shell", () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, NoFluidMobBucketWithVariantsItem> SNAIL_BUCKET = ITEMS.register("snail_bucket", () -> new NoFluidMobBucketWithVariantsItem(NaturalistEntityTypes.SNAIL, () -> Fluids.EMPTY, NaturalistSoundEvents.BUCKET_EMPTY_SNAIL, new Item.Properties().stacksTo(1), Snail.Color.values().length));

    public static final DeferredHolder<Block, TransparentBlock> AZURE_FROGLASS = registerBlock("azure_froglass", () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredHolder<Block, TransparentBlock> VERDANT_FROGLASS = registerBlock("verdant_froglass", () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredHolder<Block, TransparentBlock> CRIMSON_FROGLASS = registerBlock("crimson_froglass", () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)));
    public static final DeferredHolder<Block, IronBarsBlock> AZURE_FROGLASS_PANE = registerBlock("azure_froglass_pane", () -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)));
    public static final DeferredHolder<Block, IronBarsBlock> VERDANT_FROGLASS_PANE = registerBlock("verdant_froglass_pane", () -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)));
    public static final DeferredHolder<Block, IronBarsBlock> CRIMSON_FROGLASS_PANE = registerBlock("crimson_froglass_pane", () -> new IronBarsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE)));
    public static final DeferredHolder<Block, Block> SHELLSTONE = registerBlock("shellstone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, StairBlock> SHELLSTONE_STAIRS = registerBlock("shellstone_stairs", () -> new StairBlock(SHELLSTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, SlabBlock> SHELLSTONE_SLAB = registerBlock("shellstone_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, WallBlock> SHELLSTONE_WALL = registerBlock("shellstone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, Block> SHELLSTONE_BRICKS = registerBlock("shellstone_bricks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, StairBlock> SHELLSTONE_BRICK_STAIRS = registerBlock("shellstone_brick_stairs", () -> new StairBlock(SHELLSTONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, SlabBlock> SHELLSTONE_BRICK_SLAB = registerBlock("shellstone_brick_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, WallBlock> SHELLSTONE_BRICK_WALL = registerBlock("shellstone_brick_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, Block> CUT_SHELLSTONE = registerBlock("cut_shellstone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, StairBlock> CUT_SHELLSTONE_STAIRS = registerBlock("cut_shellstone_stairs", () -> new StairBlock(CUT_SHELLSTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, SlabBlock> CUT_SHELLSTONE_SLAB = registerBlock("cut_shellstone_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, WallBlock> CUT_SHELLSTONE_WALL = registerBlock("cut_shellstone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, Block> SMOOTH_SHELLSTONE = registerBlock("smooth_shellstone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, StairBlock> SMOOTH_SHELLSTONE_STAIRS = registerBlock("smooth_shellstone_stairs", () -> new StairBlock(SMOOTH_SHELLSTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, SlabBlock> SMOOTH_SHELLSTONE_SLAB = registerBlock("smooth_shellstone_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));
    public static final DeferredHolder<Block, WallBlock> SMOOTH_SHELLSTONE_WALL = registerBlock("smooth_shellstone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SANDSTONE)));

    public static final DeferredHolder<Item, DeferredSpawnEggItem> ALLIGATOR_SPAWN_EGG = ITEMS.register("alligator_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.ALLIGATOR, 6184228, 13810273, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BASS_SPAWN_EGG = ITEMS.register("bass_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.BASS, 8159273, 14729339, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BEAR_SPAWN_EGG = ITEMS.register("bear_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.BEAR, 6569255, 13150577, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BLUEJAY_SPAWN_EGG = ITEMS.register("bluejay_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.BLUEJAY, 2830129, 4289464, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BOAR_SPAWN_EGG = ITEMS.register("boar_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.BOAR, 6768433, 9854549, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> BUTTERFLY_SPAWN_EGG = ITEMS.register("butterfly_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.BUTTERFLY, 15165706, 6828564, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CANARY_SPAWN_EGG = ITEMS.register("canary_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.CANARY, 16704333, 13999625, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CARDINAL_SPAWN_EGG = ITEMS.register("cardinal_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.CARDINAL, 13772840, 4465186, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CATFISH_SPAWN_EGG = ITEMS.register("catfish_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.CATFISH, 8416033, 12233092, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CATERPILLAR_SPAWN_EGG = ITEMS.register("caterpillar_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.CATERPILLAR, 3815473, 15647488, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> CORAL_SNAKE_SPAWN_EGG = ITEMS.register("coral_snake_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.CORAL_SNAKE, 3485226, 12261376, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DEER_SPAWN_EGG = ITEMS.register("deer_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.DEER, 10318165, 14531208, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DRAGONFLY_SPAWN_EGG = ITEMS.register("dragonfly_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.DRAGONFLY, 7507200, 16771840, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> DUCK_SPAWN_EGG = ITEMS.register("duck_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.DUCK, 13286315, 2333491, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ELEPHANT_SPAWN_EGG = ITEMS.register("elephant_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.ELEPHANT, 9539213, 6643034, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> FINCH_SPAWN_EGG = ITEMS.register("finch_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.FINCH, 12013877, 6576975, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> FIREFLY_SPAWN_EGG = ITEMS.register("firefly_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.FIREFLY, 6764577, 16768800, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> GIRAFFE_SPAWN_EGG = ITEMS.register("giraffe_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.GIRAFFE, 14329967, 7619616, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> HIPPO_SPAWN_EGG = ITEMS.register("hippo_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.HIPPO, 15702682, 9004386, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> LION_SPAWN_EGG = ITEMS.register("lion_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.LION, 14990722, 6699537, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> LIZARD_SPAWN_EGG = ITEMS.register("lizard_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.LIZARD, 10853166, 15724462, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> RATTLESNAKE_SPAWN_EGG = ITEMS.register("rattlesnake_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.RATTLESNAKE, 16039772, 7293214, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> RHINO_SPAWN_EGG = ITEMS.register("rhino_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.RHINO, 7626842, 10982025, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ROBIN_SPAWN_EGG = ITEMS.register("robin_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.ROBIN, 4865860, 16620592, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SNAKE_SPAWN_EGG = ITEMS.register("snake_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.SNAKE, 8813107, 15524255, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SNAIL_SPAWN_EGG = ITEMS.register("snail_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.SNAIL, 5457209, 8811878, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> SPARROW_SPAWN_EGG = ITEMS.register("sparrow_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.SPARROW, 6504493, 14603707, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> TORTOISE_SPAWN_EGG = ITEMS.register("tortoise_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.TORTOISE, 15724462, 11765582, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> VULTURE_SPAWN_EGG = ITEMS.register("vulture_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.VULTURE, 4010022, 15325376, new Item.Properties()));
    public static final DeferredHolder<Item, DeferredSpawnEggItem> ZEBRA_SPAWN_EGG = ITEMS.register("zebra_spawn_egg", () -> new DeferredSpawnEggItem(NaturalistEntityTypes.ZEBRA, 15263457, 1710104, new Item.Properties()));

    public static void init() {
    }

    private static <T extends Block> DeferredHolder<Block, T> registerBlock(String name, Supplier<T> block) {
        DeferredHolder<Block, T> holder = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(holder.get(), new Item.Properties()));
        return holder;
    }

    private static <T extends Block> DeferredHolder<Block, T> registerBlockOnly(String name, Supplier<T> block) {
        return BLOCKS.register(name, block);
    }
}
