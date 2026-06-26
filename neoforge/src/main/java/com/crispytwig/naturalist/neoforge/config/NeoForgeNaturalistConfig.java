package com.crispytwig.naturalist.neoforge.config;

import com.crispytwig.naturalist.NaturalistConfig;
import com.crispytwig.naturalist.platform.services.IConfigHelper;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

public final class NeoForgeNaturalistConfig implements IConfigHelper {
    public static final ModConfigSpec SPEC;
    private static final Map<String, ModConfigSpec.BooleanValue> VALUES = new HashMap<>();
    private static final ModConfigSpec.BooleanValue SNAIL_CRUSHING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("naturalist.configuration.disable_mobs").push("disable_mobs");
        for (String key : NaturalistConfig.MOB_KEYS) {
            String name = NaturalistConfig.configKey(key);
            VALUES.put(key, builder.translation("naturalist.configuration." + name).define(name, false));
        }
        builder.pop();

        builder.translation("naturalist.configuration.behavior").push("behavior");
        SNAIL_CRUSHING = builder
                .translation("naturalist.configuration." + NaturalistConfig.SNAIL_CRUSHING_KEY)
                .comment("Unnamed Snails will be crushed when falling on them if not wearing Feather Falling boots.")
                .define(NaturalistConfig.SNAIL_CRUSHING_KEY, false);
        builder.pop();

        SPEC = builder.build();
    }

    @Override
    public boolean isMobRemoved(String canonicalKey) {
        ModConfigSpec.BooleanValue value = VALUES.get(canonicalKey);
        return value != null && value.get();
    }

    @Override
    public boolean isSnailCrushingEnabled() {
        return SNAIL_CRUSHING.get();
    }
}
