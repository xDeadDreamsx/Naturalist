package com.starfish_studios.naturalist;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class NaturalistConfig {
    public static final ModConfigSpec COMMON_CONFIG_SPEC;
    public static final NaturalistConfig COMMON_CONFIG;

    public final ModConfigSpec.BooleanValue alligatorRemoved;
    public final ModConfigSpec.BooleanValue bassRemoved;
    public final ModConfigSpec.BooleanValue bearRemoved;
    public final ModConfigSpec.BooleanValue birdRemoved;
    public final ModConfigSpec.BooleanValue bluejayRemoved;
    public final ModConfigSpec.BooleanValue canaryRemoved;
    public final ModConfigSpec.BooleanValue cardinalRemoved;
    public final ModConfigSpec.BooleanValue finchRemoved;
    public final ModConfigSpec.BooleanValue robinRemoved;
    public final ModConfigSpec.BooleanValue sparrowRemoved;
    public final ModConfigSpec.BooleanValue boarRemoved;
    public final ModConfigSpec.BooleanValue butterflyRemoved;
    public final ModConfigSpec.BooleanValue catfishRemoved;
    public final ModConfigSpec.BooleanValue deerRemoved;
    public final ModConfigSpec.BooleanValue dragonflyRemoved;
    public final ModConfigSpec.BooleanValue duckRemoved;
    public final ModConfigSpec.BooleanValue elephantRemoved;
    public final ModConfigSpec.BooleanValue fireflyRemoved;
    public final ModConfigSpec.BooleanValue forestFoxRemoved;
    public final ModConfigSpec.BooleanValue forestRabbitRemoved;
    public final ModConfigSpec.BooleanValue giraffeRemoved;
    public final ModConfigSpec.BooleanValue hippoRemoved;
    public final ModConfigSpec.BooleanValue lionRemoved;
    public final ModConfigSpec.BooleanValue lizardRemoved;
    public final ModConfigSpec.BooleanValue rhinoRemoved;
    public final ModConfigSpec.BooleanValue snailRemoved;
    public final ModConfigSpec.BooleanValue snakeRemoved;
    public final ModConfigSpec.BooleanValue coralSnakeRemoved;
    public final ModConfigSpec.BooleanValue rattlesnakeRemoved;
    public final ModConfigSpec.BooleanValue tortoiseRemoved;
    public final ModConfigSpec.BooleanValue vultureRemoved;
    public final ModConfigSpec.BooleanValue zebraRemoved;

    static {
        final Pair<NaturalistConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(NaturalistConfig::new);
        COMMON_CONFIG = pair.getLeft();
        COMMON_CONFIG_SPEC = pair.getRight();
    }

    public NaturalistConfig(ModConfigSpec.Builder builder) {
        builder.translation("naturalist.configuration.disable_mobs").push("disable_mobs");
        alligatorRemoved = builder.translation("naturalist.configuration.alligator_removed").define("alligator_removed", false);
        bassRemoved = builder.translation("naturalist.configuration.bass_removed").define("bass_removed", false);
        bearRemoved = builder.translation("naturalist.configuration.bear_removed").define("bear_removed", false);
        birdRemoved = builder.translation("naturalist.configuration.bird_removed").define("bird_removed", false);
        bluejayRemoved = birdRemoved;
        canaryRemoved = birdRemoved;
        cardinalRemoved = birdRemoved;
        finchRemoved = birdRemoved;
        robinRemoved = birdRemoved;
        sparrowRemoved = birdRemoved;
        boarRemoved = builder.translation("naturalist.configuration.boar_removed").define("boar_removed", false);
        butterflyRemoved = builder.translation("naturalist.configuration.butterfly_removed").define("butterfly_removed", false);
        catfishRemoved = builder.translation("naturalist.configuration.catfish_removed").define("catfish_removed", false);
        deerRemoved = builder.translation("naturalist.configuration.deer_removed").define("deer_removed", false);
        dragonflyRemoved = builder.translation("naturalist.configuration.dragonfly_removed").define("dragonfly_removed", false);
        duckRemoved = builder.translation("naturalist.configuration.duck_removed").define("duck_removed", false);
        elephantRemoved = builder.translation("naturalist.configuration.elephant_removed").define("elephant_removed", false);
        fireflyRemoved = builder.translation("naturalist.configuration.firefly_removed").define("firefly_removed", false);
        forestFoxRemoved = builder.translation("naturalist.configuration.forest_fox_removed").define("forest_fox_removed", false);
        forestRabbitRemoved = builder.translation("naturalist.configuration.forest_rabbit_removed").define("forest_rabbit_removed", false);
        giraffeRemoved = builder.translation("naturalist.configuration.giraffe_removed").define("giraffe_removed", false);
        hippoRemoved = builder.translation("naturalist.configuration.hippo_removed").define("hippo_removed", false);
        lionRemoved = builder.translation("naturalist.configuration.lion_removed").define("lion_removed", false);
        lizardRemoved = builder.translation("naturalist.configuration.lizard_removed").define("lizard_removed", false);
        rhinoRemoved = builder.translation("naturalist.configuration.rhino_removed").define("rhino_removed", false);
        snailRemoved = builder.translation("naturalist.configuration.snail_removed").define("snail_removed", false);
        snakeRemoved = builder.translation("naturalist.configuration.snake_removed").define("snake_removed", false);
        coralSnakeRemoved = snakeRemoved;
        rattlesnakeRemoved = snakeRemoved;
        tortoiseRemoved = builder.translation("naturalist.configuration.tortoise_removed").define("tortoise_removed", false);
        vultureRemoved = builder.translation("naturalist.configuration.vulture_removed").define("vulture_removed", false);
        zebraRemoved = builder.translation("naturalist.configuration.zebra_removed").define("zebra_removed", false);
        builder.pop();
    }
}
