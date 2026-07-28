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
        builder.pop();

        SPEC = builder.build();
    }

    private static void define(ModConfigSpec.Builder builder, List<String> keys) {
        for (String key : keys) {
            String name = NaturalistConfig.configKey(key);
            VALUES.put(key, builder.translation("naturalist.configuration." + name).define(name, false));
        }
    }

    @Override
    public boolean isMobRemoved(String canonicalKey) {
        ModConfigSpec.BooleanValue value = VALUES.get(canonicalKey);
        return value != null && value.get();
    }

    @Override
    public boolean areAllBugsRemoved() {
        return REMOVE_ALL_BUGS.get();
    }

    @Override
    public boolean isSnailCrushingEnabled() {
        return SNAIL_CRUSHING.get();
    }
}
