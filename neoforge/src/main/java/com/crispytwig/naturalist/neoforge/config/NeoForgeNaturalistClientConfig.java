package com.crispytwig.naturalist.neoforge.config;

import com.crispytwig.naturalist.NaturalistClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class NeoForgeNaturalistClientConfig {
    public static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue GLOW_GOOP_TOOLTIP;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        GLOW_GOOP_TOOLTIP = builder
                .translation("naturalist.configuration." + NaturalistClientConfig.GLOW_GOOP_TOOLTIP_KEY)
                .define(NaturalistClientConfig.GLOW_GOOP_TOOLTIP_KEY, true);

        SPEC = builder.build();
    }

    private NeoForgeNaturalistClientConfig() {}

    public static boolean isGlowGoopTooltipEnabled() {
        return !SPEC.isLoaded() || GLOW_GOOP_TOOLTIP.get();
    }
}
