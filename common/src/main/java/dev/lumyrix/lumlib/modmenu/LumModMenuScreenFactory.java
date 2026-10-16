package dev.lumyrix.lumlib.modmenu;

import dev.lumyrix.lumlib.api.LumConfigContainer;
import dev.lumyrix.lumlib.config.LumConfigRegistry;
import dev.lumyrix.lumlib.gui.LumConfigScreen;
import net.minecraft.client.gui.screens.Screen;

/**
 * Reusable ModMenu {@code ConfigScreenFactory} target for any mod that
 * registers with LumLib. A consuming mod wires this up as its own ModMenu
 * entrypoint, passing its own mod id, so ModMenu shows a normal per-mod
 * config entry — LumLib itself never appears as a mod in ModMenu's list.
 *
 * <pre>{@code
 * // fabric.mod.json of the CONSUMING mod (not LumLib):
 * "entrypoints": {
 *   "modmenu": [ "com.example.mymod.MyModMenuIntegration" ]
 * }
 *
 * // MyModMenuIntegration.java in the consuming mod:
 * public class MyModMenuIntegration implements ModMenuApi {
 *     public ConfigScreenFactory<?> getModConfigScreenFactory() {
 *         return LumModMenuScreenFactory.forMod("mymod");
 *     }
 * }
 * }</pre>
 */
public final class LumModMenuScreenFactory {

    private LumModMenuScreenFactory() {
    }

    /**
     * @param modId the id passed to {@code LumLib.registerMod(modId, ...)}
     * @return a function suitable for {@code ModMenuApi.getModConfigScreenFactory()}
     */
    public static java.util.function.Function<Screen, Screen> forMod(String modId) {
        return parent -> {
            LumConfigContainer container = LumConfigRegistry.get(modId);
            if (container == null) {
                throw new IllegalStateException(
                        "No mod registered with LumLib under id '" + modId + "'. " +
                        "Call LumLib.registerMod(\"" + modId + "\", ...).build() before ModMenu opens this screen.");
            }
            return new LumConfigScreen(parent, container);
        };
    }
}
