package com.starfish_studios.naturalist;

import com.starfish_studios.naturalist.platform.Services;

import java.util.List;
import java.util.Map;

public final class NaturalistConfig {
    public static final List<String> MOB_KEYS = List.of(
            "alligator", "bass", "bear", "bird", "boar", "butterfly", "catfish", "deer",
            "dragonfly", "duck", "elephant", "firefly", "forestFox", "forestRabbit", "giraffe",
            "hippo", "lion", "lizard", "rhino", "snail", "snake", "tortoise", "vulture", "zebra"
    );

    private static final Map<String, String> ALIASES = Map.of(
            "bluejay", "bird",
            "canary", "bird",
            "cardinal", "bird",
            "finch", "bird",
            "robin", "bird",
            "sparrow", "bird",
            "coralSnake", "snake",
            "rattlesnake", "snake"
    );

    private NaturalistConfig() {}

    public static String canonicalKey(String mobName) {
        return ALIASES.getOrDefault(mobName, mobName);
    }

    public static String configKey(String mobKey) {
        StringBuilder sb = new StringBuilder();
        for (char c : mobKey.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.append("_removed").toString();
    }

    public static boolean isRemoved(String mobName) {
        return Services.CONFIG.isMobRemoved(canonicalKey(mobName));
    }
}
