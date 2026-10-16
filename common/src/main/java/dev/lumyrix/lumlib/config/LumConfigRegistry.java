package dev.lumyrix.lumlib.config;

import dev.lumyrix.lumlib.api.LumConfigContainer;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory registry of every mod that has called {@code LumLib.registerMod(...)}.
 * Insertion order is preserved so the mod list screen shows mods in the order
 * they initialized, which is stable across a given launch.
 */
public final class LumConfigRegistry {

    private static final Map<String, LumConfigContainer> CONTAINERS = new LinkedHashMap<>();

    private LumConfigRegistry() {
    }

    public static void register(LumConfigContainer container) {
        CONTAINERS.put(container.modId(), container);
    }

    public static boolean isRegistered(String modId) {
        return CONTAINERS.containsKey(modId);
    }

    public static LumConfigContainer get(String modId) {
        return CONTAINERS.get(modId);
    }

    public static Collection<LumConfigContainer> all() {
        return CONTAINERS.values();
    }
}
