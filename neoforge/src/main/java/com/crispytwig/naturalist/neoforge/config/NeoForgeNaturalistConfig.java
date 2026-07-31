package com.crispytwig.naturalist.neoforge.config;

import com.crispytwig.naturalist.NaturalistConfig;
import com.crispytwig.naturalist.platform.services.IConfigHelper;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class NeoForgeNaturalistConfig implements IConfigHelper {
    public static final ModConfigSpec SPEC;
    private static final Map<String, ModConfigSpec.BooleanValue> VALUES = new HashMap<>();
    private static final ModConfigSpec.BooleanValue SNAIL_CRUSHING;
    private static final ModConfigSpec.BooleanValue REMOVE_ALL_BUGS;
    private static final ModConfigSpec.BooleanValue BIRD_HEAD_SLOW_FALLING;
    private static final ModConfigSpec.BooleanValue PARROT_FLIGHT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("naturalist.configuration.disable_mobs").push("disable_mobs");

        builder.translation("naturalist.configuration.aquatic_mobs").push("aquatic_mobs");
        define(builder, NaturalistConfig.AQUATIC_MOBS);
        builder.pop();

        builder.translation("naturalist.configuration.land_mobs").push("land_mobs");
        define(builder, NaturalistConfig.LAND_MOBS);
        builder.pop();

        REMOVE_ALL_BUGS = builder
                .translation("naturalist.configuration." + NaturalistConfig.REMOVE_ALL_BUGS_KEY)
                .define(NaturalistConfig.REMOVE_ALL_BUGS_KEY, false);

        builder.translation("naturalist.configuration.bugs").push("bugs");
        define(builder, NaturalistConfig.BUGS);
        builder.pop();

        builder.pop();

        builder.translation("naturalist.configuration.behavior").push("behavior");
        SNAIL_CRUSHING = builder
                .translation("naturalist.configuration." + NaturalistConfig.SNAIL_CRUSHING_KEY)
                .define(NaturalistConfig.SNAIL_CRUSHING_KEY, false);
        BIRD_HEAD_SLOW_FALLING = builder
                .translation("naturalist.configuration." + NaturalistConfig.BIRD_HEAD_SLOW_FALLING_KEY)
                .define(NaturalistConfig.BIRD_HEAD_SLOW_FALLING_KEY, true);
        PARROT_FLIGHT = builder
                .translation("naturalist.configuration." + NaturalistConfig.PARROT_FLIGHT_KEY)
                .define(NaturalistConfig.PARROT_FLIGHT_KEY, true);
        builder.pop();

        SPEC = builder.build();
    }

    private static void define(ModConfigSpec.Builder builder, List<String> keys) {
        for (String key : keys) {
            String name = NaturalistConfig.configKey(key);
            VALUES.put(key, builder.translation("naturalist.configuration." + name).define(name, false));
        }
    }

    private static boolean read(ModConfigSpec.BooleanValue value, boolean fallback) {
        if (value == null || !SPEC.isLoaded()) {
            return fallback;
        }
        return value.get();
    }

    @Override
    public boolean isMobRemoved(String canonicalKey) {
        return read(VALUES.get(canonicalKey), false);
    }

    @Override
    public boolean areAllBugsRemoved() {
        return read(REMOVE_ALL_BUGS, false);
    }

    @Override
    public boolean isSnailCrushingEnabled() {
        return read(SNAIL_CRUSHING, false);
    }

    @Override
    public boolean isBirdHeadSlowFallingEnabled() {
        return read(BIRD_HEAD_SLOW_FALLING, true);
    }

    @Override
    public boolean isParrotFlightEnabled() {
        return read(PARROT_FLIGHT, true);
    }
}
