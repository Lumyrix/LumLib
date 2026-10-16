package dev.lumyrix.lumlib.api;

import dev.lumyrix.lumlib.config.LumConfigRegistry;

/**
 * Public entry point for mods integrating with LumLib.
 * <p>
 * Call {@link #registerMod(String, String)} once from your client entrypoint
 * to obtain a {@link LumConfigContainer}, add your options to it, then call
 * {@link LumConfigContainer#build()}. LumLib takes care of the screen, JSON
 * persistence, ModMenu integration, and the pause-screen fallback button.
 *
 * <pre>{@code
 * public class MyModConfig {
 *     public static void register() {
 *         LumConfigContainer c = LumLib.registerMod("mymod", "My Mod");
 *         c.addBoolean("enabled", "Enabled", true, v -> Config.enabled = v, () -> Config.enabled);
 *         c.build();
 *     }
 * }
 * }</pre>
 */
public final class LumLib {

    public static final String MOD_ID = "lumlib";

    private LumLib() {
    }

    /**
     * Registers a mod with LumLib and returns a container to add options to.
     *
     * @param modId       the mod's fabric.mod.json id, used as the JSON config filename
     * @param displayName the human-readable name shown in LumLib's mod list
     * @return a fresh {@link LumConfigContainer} for this mod
     */
    public static LumConfigContainer registerMod(String modId, String displayName) {
        if (modId == null || modId.isBlank()) {
            throw new IllegalArgumentException("modId must not be blank");
        }
        if (LumConfigRegistry.isRegistered(modId)) {
            throw new IllegalStateException("Mod '" + modId + "' already registered with LumLib");
        }
        return new LumConfigContainer(modId, displayName == null || displayName.isBlank() ? modId : displayName);
    }

    /**
     * @param modId the mod id to check
     * @return true if a mod with this id has already called {@link #registerMod}
     */
    public static boolean isRegistered(String modId) {
        return LumConfigRegistry.isRegistered(modId);
    }
}
