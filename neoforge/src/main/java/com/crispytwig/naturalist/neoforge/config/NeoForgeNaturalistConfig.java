package com.crispytwig.naturalist.neoforge.config;

import com.crispytwig.naturalist.NaturalistConfig;
import com.crispytwig.naturalist.platform.services.IConfigHelper;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

public final class NeoForgeNaturalistConfig implements IConfigHelper {
    public static final ModConfigSpec SPEC;
    private static final Map<String, ModConfigSpec.BooleanValue> VALUES = new HashMap<>();

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.translation("naturalist.configuration.disable_mobs").push("disable_mobs");
        for (String key : NaturalistConfig.MOB_KEYS) {
            String name = NaturalistConfig.configKey(key);
            VALUES.put(key, builder.translation("naturalist.configuration." + name).define(name, false));
        }
        builder.pop();
        SPEC = builder.build();
    }

    @Override
    public boolean isMobRemoved(String canonicalKey) {
        ModConfigSpec.BooleanValue value = VALUES.get(canonicalKey);
        return value != null && value.get();
    }
}
