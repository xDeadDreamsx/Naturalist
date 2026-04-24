package com.starfish_studios.naturalist;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class NaturalistConfig {
    public static final ModConfigSpec COMMON_CONFIG_SPEC;
    public static final NaturalistConfig COMMON_CONFIG;

    public final ModConfigSpec.BooleanValue alligatorRemoved;
    public final ModConfigSpec.BooleanValue bassRemoved;
    public final ModConfigSpec.BooleanValue bearRemoved;
    public final ModConfigSpec.BooleanValue bluejayRemoved;
    public final ModConfigSpec.BooleanValue boarRemoved;
    public final ModConfigSpec.BooleanValue butterflyRemoved;
    public final ModConfigSpec.BooleanValue canaryRemoved;
    public final ModConfigSpec.BooleanValue cardinalRemoved;
    public final ModConfigSpec.BooleanValue catfishRemoved;
    public final ModConfigSpec.BooleanValue coralSnakeRemoved;
    public final ModConfigSpec.BooleanValue deerRemoved;
    public final ModConfigSpec.BooleanValue dragonflyRemoved;
    public final ModConfigSpec.BooleanValue duckRemoved;
    public final ModConfigSpec.BooleanValue elephantRemoved;
    public final ModConfigSpec.BooleanValue finchRemoved;
    public final ModConfigSpec.BooleanValue fireflyRemoved;
    public final ModConfigSpec.BooleanValue forestFoxRemoved;
    public final ModConfigSpec.BooleanValue forestRabbitRemoved;
    public final ModConfigSpec.BooleanValue giraffeRemoved;
    public final ModConfigSpec.BooleanValue hippoRemoved;
    public final ModConfigSpec.BooleanValue lionRemoved;
    public final ModConfigSpec.BooleanValue lizardRemoved;
    public final ModConfigSpec.BooleanValue rattlesnakeRemoved;
    public final ModConfigSpec.BooleanValue rhinoRemoved;
    public final ModConfigSpec.BooleanValue robinRemoved;
    public final ModConfigSpec.BooleanValue snailRemoved;
    public final ModConfigSpec.BooleanValue snakeRemoved;
    public final ModConfigSpec.BooleanValue sparrowRemoved;
    public final ModConfigSpec.BooleanValue tortoiseRemoved;
    public final ModConfigSpec.BooleanValue vultureRemoved;
    public final ModConfigSpec.BooleanValue zebraRemoved;

    public final ModConfigSpec.IntValue alligatorSpawnWeight;
    public final ModConfigSpec.IntValue alligatorSpawnMinGroupSize;
    public final ModConfigSpec.IntValue alligatorSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue bassSpawnWeight;
    public final ModConfigSpec.IntValue bassSpawnMinGroupSize;
    public final ModConfigSpec.IntValue bassSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue bearSpawnWeight;
    public final ModConfigSpec.IntValue bearSpawnMinGroupSize;
    public final ModConfigSpec.IntValue bearSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue bluejaySpawnWeight;
    public final ModConfigSpec.IntValue bluejaySpawnMinGroupSize;
    public final ModConfigSpec.IntValue bluejaySpawnMaxGroupSize;

    public final ModConfigSpec.IntValue boarSpawnWeight;
    public final ModConfigSpec.IntValue boarSpawnMinGroupSize;
    public final ModConfigSpec.IntValue boarSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue butterflySpawnWeight;
    public final ModConfigSpec.IntValue butterflySpawnMinGroupSize;
    public final ModConfigSpec.IntValue butterflySpawnMaxGroupSize;

    public final ModConfigSpec.IntValue canarySpawnWeight;
    public final ModConfigSpec.IntValue canarySpawnMinGroupSize;
    public final ModConfigSpec.IntValue canarySpawnMaxGroupSize;

    public final ModConfigSpec.IntValue cardinalSpawnWeight;
    public final ModConfigSpec.IntValue cardinalSpawnMinGroupSize;
    public final ModConfigSpec.IntValue cardinalSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue catfishSpawnWeight;
    public final ModConfigSpec.IntValue catfishSpawnMinGroupSize;
    public final ModConfigSpec.IntValue catfishSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue coralSnakeSpawnWeight;
    public final ModConfigSpec.IntValue coralSnakeSpawnMinGroupSize;
    public final ModConfigSpec.IntValue coralSnakeSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue deerSpawnWeight;
    public final ModConfigSpec.IntValue deerSpawnMinGroupSize;
    public final ModConfigSpec.IntValue deerSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue dragonflySpawnWeight;
    public final ModConfigSpec.IntValue dragonflySpawnMinGroupSize;
    public final ModConfigSpec.IntValue dragonflySpawnMaxGroupSize;

    public final ModConfigSpec.IntValue duckSpawnWeight;
    public final ModConfigSpec.IntValue duckSpawnMinGroupSize;
    public final ModConfigSpec.IntValue duckSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue elephantSpawnWeight;
    public final ModConfigSpec.IntValue elephantSpawnMinGroupSize;
    public final ModConfigSpec.IntValue elephantSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue finchSpawnWeight;
    public final ModConfigSpec.IntValue finchSpawnMinGroupSize;
    public final ModConfigSpec.IntValue finchSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue fireflySpawnWeight;
    public final ModConfigSpec.IntValue fireflySpawnMinGroupSize;
    public final ModConfigSpec.IntValue fireflySpawnMaxGroupSize;

    public final ModConfigSpec.IntValue forestFoxSpawnWeight;
    public final ModConfigSpec.IntValue forestFoxSpawnMinGroupSize;
    public final ModConfigSpec.IntValue forestFoxSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue forestRabbitSpawnWeight;
    public final ModConfigSpec.IntValue forestRabbitSpawnMinGroupSize;
    public final ModConfigSpec.IntValue forestRabbitSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue giraffeSpawnWeight;
    public final ModConfigSpec.IntValue giraffeSpawnMinGroupSize;
    public final ModConfigSpec.IntValue giraffeSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue hippoSpawnWeight;
    public final ModConfigSpec.IntValue hippoSpawnMinGroupSize;
    public final ModConfigSpec.IntValue hippoSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue lionSpawnWeight;
    public final ModConfigSpec.IntValue lionSpawnMinGroupSize;
    public final ModConfigSpec.IntValue lionSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue lizardSpawnWeight;
    public final ModConfigSpec.IntValue lizardSpawnMinGroupSize;
    public final ModConfigSpec.IntValue lizardSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue rattlesnakeSpawnWeight;
    public final ModConfigSpec.IntValue rattlesnakeSpawnMinGroupSize;
    public final ModConfigSpec.IntValue rattlesnakeSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue rhinoSpawnWeight;
    public final ModConfigSpec.IntValue rhinoSpawnMinGroupSize;
    public final ModConfigSpec.IntValue rhinoSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue robinSpawnWeight;
    public final ModConfigSpec.IntValue robinSpawnMinGroupSize;
    public final ModConfigSpec.IntValue robinSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue snailSpawnWeight;
    public final ModConfigSpec.IntValue snailSpawnMinGroupSize;
    public final ModConfigSpec.IntValue snailSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue snakeSpawnWeight;
    public final ModConfigSpec.IntValue snakeSpawnMinGroupSize;
    public final ModConfigSpec.IntValue snakeSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue sparrowSpawnWeight;
    public final ModConfigSpec.IntValue sparrowSpawnMinGroupSize;
    public final ModConfigSpec.IntValue sparrowSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue tortoiseSpawnWeight;
    public final ModConfigSpec.IntValue tortoiseSpawnMinGroupSize;
    public final ModConfigSpec.IntValue tortoiseSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue vultureSpawnWeight;
    public final ModConfigSpec.IntValue vultureSpawnMinGroupSize;
    public final ModConfigSpec.IntValue vultureSpawnMaxGroupSize;

    public final ModConfigSpec.IntValue zebraSpawnWeight;
    public final ModConfigSpec.IntValue zebraSpawnMinGroupSize;
    public final ModConfigSpec.IntValue zebraSpawnMaxGroupSize;

    static {
        final Pair<NaturalistConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(NaturalistConfig::new);
        COMMON_CONFIG = pair.getLeft();
        COMMON_CONFIG_SPEC = pair.getRight();
    }

    public NaturalistConfig(ModConfigSpec.Builder builder) {
        builder.push("mobRemoval");
        alligatorRemoved = builder.define("alligatorRemoved", false);
        bassRemoved = builder.define("bassRemoved", false);
        bearRemoved = builder.define("bearRemoved", false);
        bluejayRemoved = builder.define("bluejayRemoved", false);
        boarRemoved = builder.define("boarRemoved", false);
        butterflyRemoved = builder.define("butterflyRemoved", false);
        canaryRemoved = builder.define("canaryRemoved", false);
        cardinalRemoved = builder.define("cardinalRemoved", false);
        catfishRemoved = builder.define("catfishRemoved", false);
        coralSnakeRemoved = builder.define("coralSnakeRemoved", false);
        deerRemoved = builder.define("deerRemoved", false);
        dragonflyRemoved = builder.define("dragonflyRemoved", false);
        duckRemoved = builder.define("duckRemoved", false);
        elephantRemoved = builder.define("elephantRemoved", false);
        finchRemoved = builder.define("finchRemoved", false);
        fireflyRemoved = builder.define("fireflyRemoved", false);
        forestFoxRemoved = builder.define("forestFoxRemoved", false);
        forestRabbitRemoved = builder.define("forestRabbitRemoved", false);
        giraffeRemoved = builder.define("giraffeRemoved", false);
        hippoRemoved = builder.define("hippoRemoved", false);
        lionRemoved = builder.define("lionRemoved", false);
        lizardRemoved = builder.define("lizardRemoved", false);
        rattlesnakeRemoved = builder.define("rattlesnakeRemoved", false);
        rhinoRemoved = builder.define("rhinoRemoved", false);
        robinRemoved = builder.define("robinRemoved", false);
        snailRemoved = builder.define("snailRemoved", false);
        snakeRemoved = builder.define("snakeRemoved", false);
        sparrowRemoved = builder.define("sparrowRemoved", false);
        tortoiseRemoved = builder.define("tortoiseRemoved", false);
        vultureRemoved = builder.define("vultureRemoved", false);
        zebraRemoved = builder.define("zebraRemoved", false);
        builder.pop();

        builder.push("mobConfig");
        alligatorSpawnWeight = builder.defineInRange("alligatorSpawnWeight", 10, 0, 100);
        alligatorSpawnMinGroupSize = builder.defineInRange("alligatorSpawnMinGroupSize", 2, 1, 8);
        alligatorSpawnMaxGroupSize = builder.defineInRange("alligatorSpawnMaxGroupSize", 3, 1, 8);

        bassSpawnWeight = builder.defineInRange("bassSpawnWeight", 10, 0, 100);
        bassSpawnMinGroupSize = builder.defineInRange("bassSpawnMinGroupSize", 3, 1, 8);
        bassSpawnMaxGroupSize = builder.defineInRange("bassSpawnMaxGroupSize", 6, 1, 8);

        bearSpawnWeight = builder.defineInRange("bearSpawnWeight", 10, 0, 100);
        bearSpawnMinGroupSize = builder.defineInRange("bearSpawnMinGroupSize", 1, 1, 8);
        bearSpawnMaxGroupSize = builder.defineInRange("bearSpawnMaxGroupSize", 2, 1, 8);

        bluejaySpawnWeight = builder.defineInRange("bluejaySpawnWeight", 10, 0, 100);
        bluejaySpawnMinGroupSize = builder.defineInRange("bluejaySpawnMinGroupSize", 3, 1, 8);
        bluejaySpawnMaxGroupSize = builder.defineInRange("bluejaySpawnMaxGroupSize", 4, 1, 8);

        boarSpawnWeight = builder.defineInRange("boarSpawnWeight", 10, 0, 100);
        boarSpawnMinGroupSize = builder.defineInRange("boarSpawnMinGroupSize", 3, 1, 8);
        boarSpawnMaxGroupSize = builder.defineInRange("boarSpawnMaxGroupSize", 4, 1, 8);

        butterflySpawnWeight = builder.defineInRange("butterflySpawnWeight", 10, 0, 100);
        butterflySpawnMinGroupSize = builder.defineInRange("butterflySpawnMinGroupSize", 3, 1, 8);
        butterflySpawnMaxGroupSize = builder.defineInRange("butterflySpawnMaxGroupSize", 6, 1, 8);

        canarySpawnWeight = builder.defineInRange("canarySpawnWeight", 10, 0, 100);
        canarySpawnMinGroupSize = builder.defineInRange("canarySpawnMinGroupSize", 3, 1, 8);
        canarySpawnMaxGroupSize = builder.defineInRange("canarySpawnMaxGroupSize", 4, 1, 8);

        cardinalSpawnWeight = builder.defineInRange("cardinalSpawnWeight", 10, 0, 100);
        cardinalSpawnMinGroupSize = builder.defineInRange("cardinalSpawnMinGroupSize", 3, 1, 8);
        cardinalSpawnMaxGroupSize = builder.defineInRange("cardinalSpawnMaxGroupSize", 4, 1, 8);

        catfishSpawnWeight = builder.defineInRange("catfishSpawnWeight", 10, 0, 100);
        catfishSpawnMinGroupSize = builder.defineInRange("catfishSpawnMinGroupSize", 1, 1, 8);
        catfishSpawnMaxGroupSize = builder.defineInRange("catfishSpawnMaxGroupSize", 2, 1, 8);

        coralSnakeSpawnWeight = builder.defineInRange("coralSnakeSpawnWeight", 10, 0, 100);
        coralSnakeSpawnMinGroupSize = builder.defineInRange("coralSnakeSpawnMinGroupSize", 1, 1, 8);
        coralSnakeSpawnMaxGroupSize = builder.defineInRange("coralSnakeSpawnMaxGroupSize", 1, 1, 8);

        deerSpawnWeight = builder.defineInRange("deerSpawnWeight", 10, 0, 100);
        deerSpawnMinGroupSize = builder.defineInRange("deerSpawnMinGroupSize", 3, 1, 8);
        deerSpawnMaxGroupSize = builder.defineInRange("deerSpawnMaxGroupSize", 5, 1, 8);

        dragonflySpawnWeight = builder.defineInRange("dragonflySpawnWeight", 10, 0, 100);
        dragonflySpawnMinGroupSize = builder.defineInRange("dragonflySpawnMinGroupSize", 2, 1, 8);
        dragonflySpawnMaxGroupSize = builder.defineInRange("dragonflySpawnMaxGroupSize", 4, 1, 8);

        duckSpawnWeight = builder.defineInRange("duckSpawnWeight", 10, 0, 100);
        duckSpawnMinGroupSize = builder.defineInRange("duckSpawnMinGroupSize", 3, 1, 8);
        duckSpawnMaxGroupSize = builder.defineInRange("duckSpawnMaxGroupSize", 4, 1, 8);

        elephantSpawnWeight = builder.defineInRange("elephantSpawnWeight", 5, 0, 100);
        elephantSpawnMinGroupSize = builder.defineInRange("elephantSpawnMinGroupSize", 1, 1, 8);
        elephantSpawnMaxGroupSize = builder.defineInRange("elephantSpawnMaxGroupSize", 3, 1, 8);

        finchSpawnWeight = builder.defineInRange("finchSpawnWeight", 10, 0, 100);
        finchSpawnMinGroupSize = builder.defineInRange("finchSpawnMinGroupSize", 3, 1, 8);
        finchSpawnMaxGroupSize = builder.defineInRange("finchSpawnMaxGroupSize", 4, 1, 8);

        fireflySpawnWeight = builder.defineInRange("fireflySpawnWeight", 10, 0, 100);
        fireflySpawnMinGroupSize = builder.defineInRange("fireflySpawnMinGroupSize", 2, 1, 8);
        fireflySpawnMaxGroupSize = builder.defineInRange("fireflySpawnMaxGroupSize", 4, 1, 8);

        forestFoxSpawnWeight = builder.defineInRange("forestFoxSpawnWeight", 10, 0, 100);
        forestFoxSpawnMinGroupSize = builder.defineInRange("forestFoxSpawnMinGroupSize", 1, 1, 8);
        forestFoxSpawnMaxGroupSize = builder.defineInRange("forestFoxSpawnMaxGroupSize", 2, 1, 8);

        forestRabbitSpawnWeight = builder.defineInRange("forestRabbitSpawnWeight", 10, 0, 100);
        forestRabbitSpawnMinGroupSize = builder.defineInRange("forestRabbitSpawnMinGroupSize", 2, 1, 8);
        forestRabbitSpawnMaxGroupSize = builder.defineInRange("forestRabbitSpawnMaxGroupSize", 3, 1, 8);

        giraffeSpawnWeight = builder.defineInRange("giraffeSpawnWeight", 5, 0, 100);
        giraffeSpawnMinGroupSize = builder.defineInRange("giraffeSpawnMinGroupSize", 1, 1, 8);
        giraffeSpawnMaxGroupSize = builder.defineInRange("giraffeSpawnMaxGroupSize", 3, 1, 8);

        hippoSpawnWeight = builder.defineInRange("hippoSpawnWeight", 10, 0, 100);
        hippoSpawnMinGroupSize = builder.defineInRange("hippoSpawnMinGroupSize", 1, 1, 8);
        hippoSpawnMaxGroupSize = builder.defineInRange("hippoSpawnMaxGroupSize", 3, 1, 8);

        lionSpawnWeight = builder.defineInRange("lionSpawnWeight", 3, 0, 100);
        lionSpawnMinGroupSize = builder.defineInRange("lionSpawnMinGroupSize", 1, 1, 8);
        lionSpawnMaxGroupSize = builder.defineInRange("lionSpawnMaxGroupSize", 3, 1, 8);

        lizardSpawnWeight = builder.defineInRange("lizardSpawnWeight", 10, 0, 100);
        lizardSpawnMinGroupSize = builder.defineInRange("lizardSpawnMinGroupSize", 1, 1, 8);
        lizardSpawnMaxGroupSize = builder.defineInRange("lizardSpawnMaxGroupSize", 1, 1, 8);

        rattlesnakeSpawnWeight = builder.defineInRange("rattlesnakeSpawnWeight", 10, 0, 100);
        rattlesnakeSpawnMinGroupSize = builder.defineInRange("rattlesnakeSpawnMinGroupSize", 1, 1, 8);
        rattlesnakeSpawnMaxGroupSize = builder.defineInRange("rattlesnakeSpawnMaxGroupSize", 1, 1, 8);

        rhinoSpawnWeight = builder.defineInRange("rhinoSpawnWeight", 1, 0, 100);
        rhinoSpawnMinGroupSize = builder.defineInRange("rhinoSpawnMinGroupSize", 1, 1, 8);
        rhinoSpawnMaxGroupSize = builder.defineInRange("rhinoSpawnMaxGroupSize", 3, 1, 8);

        robinSpawnWeight = builder.defineInRange("robinSpawnWeight", 10, 0, 100);
        robinSpawnMinGroupSize = builder.defineInRange("robinSpawnMinGroupSize", 3, 1, 8);
        robinSpawnMaxGroupSize = builder.defineInRange("robinSpawnMaxGroupSize", 4, 1, 8);

        snailSpawnWeight = builder.defineInRange("snailSpawnWeight", 10, 0, 100);
        snailSpawnMinGroupSize = builder.defineInRange("snailSpawnMinGroupSize", 2, 1, 8);
        snailSpawnMaxGroupSize = builder.defineInRange("snailSpawnMaxGroupSize", 3, 1, 8);

        snakeSpawnWeight = builder.defineInRange("snakeSpawnWeight", 10, 0, 100);
        snakeSpawnMinGroupSize = builder.defineInRange("snakeSpawnMinGroupSize", 1, 1, 8);
        snakeSpawnMaxGroupSize = builder.defineInRange("snakeSpawnMaxGroupSize", 1, 1, 8);

        sparrowSpawnWeight = builder.defineInRange("sparrowSpawnWeight", 10, 0, 100);
        sparrowSpawnMinGroupSize = builder.defineInRange("sparrowSpawnMinGroupSize", 3, 1, 8);
        sparrowSpawnMaxGroupSize = builder.defineInRange("sparrowSpawnMaxGroupSize", 4, 1, 8);

        tortoiseSpawnWeight = builder.defineInRange("tortoiseSpawnWeight", 10, 0, 100);
        tortoiseSpawnMinGroupSize = builder.defineInRange("tortoiseSpawnMinGroupSize", 1, 1, 8);
        tortoiseSpawnMaxGroupSize = builder.defineInRange("tortoiseSpawnMaxGroupSize", 1, 1, 8);

        vultureSpawnWeight = builder.defineInRange("vultureSpawnWeight", 3, 0, 100);
        vultureSpawnMinGroupSize = builder.defineInRange("vultureSpawnMinGroupSize", 3, 1, 8);
        vultureSpawnMaxGroupSize = builder.defineInRange("vultureSpawnMaxGroupSize", 5, 1, 8);

        zebraSpawnWeight = builder.defineInRange("zebraSpawnWeight", 1, 0, 100);
        zebraSpawnMinGroupSize = builder.defineInRange("zebraSpawnMinGroupSize", 2, 1, 8);
        zebraSpawnMaxGroupSize = builder.defineInRange("zebraSpawnMaxGroupSize", 6, 1, 8);
        builder.pop();
    }
}
