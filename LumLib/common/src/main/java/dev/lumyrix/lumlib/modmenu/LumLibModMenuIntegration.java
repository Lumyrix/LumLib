package dev.lumyrix.lumlib.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lumyrix.lumlib.gui.LumModListScreen;

/**
 * LumLib's own ModMenu entry. Rather than exposing settings of its own
 * (LumLib has none), it opens the same mod-list screen used as the
 * pause-screen fallback, so LumLib-registered mods are reachable from
 * ModMenu too, not just from the gear button.
 */
public final class LumLibModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LumModListScreen::new;
    }
}
