package com.crispytwig.naturalist;

import com.crispytwig.naturalist.platform.Services;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class NaturalistConfig {
    public static final List<String> AQUATIC_MOBS = List.of(
            "anglerfish", "bass", "blobfish", "catfish", "clam", "giantIsopod", "greatWhiteShark",
            "jellyfish", "piranha", "ray", "starfish", "whale"
    );

    public static final List<String> LAND_MOBS = List.of(
            "alligator", "bear", "bird", "blackBear", "boar", "capybara", "crab", "deer", "duck",
            "elephant", "forestFox", "forestRabbit", "giraffe", "hedgehog", "hippo", "komodoDragon",
            "lion", "lizard", "mammoth", "mole", "ostrich", "rat", "rhino", "snake", "tiger",
            "tortoise", "turkey", "vulture", "zebra"
    );

    public static final List<String> BUGS = List.of(
            "ant", "butterfly", "desertScorpion", "dragonfly", "firefly", "jungleScorpion", "snail"
    );

    public static final List<String> MOB_KEYS = Stream.of(AQUATIC_MOBS, LAND_MOBS, BUGS)
            .flatMap(List::stream)
            .sorted()
            .toList();

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

    public static final String SNAIL_CRUSHING_KEY = "snail_crushing";
    public static final String REMOVE_ALL_BUGS_KEY = "remove_all_bugs";
    public static final String BIRD_HEAD_SLOW_FALLING_KEY = "bird_head_slow_falling";
    public static final String PARROT_FLIGHT_KEY = "parrot_flight";

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
        String key = canonicalKey(mobName);
        if (BUGS.contains(key) && Services.CONFIG.areAllBugsRemoved()) {
            return true;
        }
        return Services.CONFIG.isMobRemoved(key);
    }

    public static boolean isSnailCrushingEnabled() {
        return Services.CONFIG.isSnailCrushingEnabled();
    }

    public static boolean isBirdHeadSlowFallingEnabled() {
        return Services.CONFIG.isBirdHeadSlowFallingEnabled();
    }

    public static boolean isParrotFlightEnabled() {
        return Services.CONFIG.isParrotFlightEnabled();
    }
}
