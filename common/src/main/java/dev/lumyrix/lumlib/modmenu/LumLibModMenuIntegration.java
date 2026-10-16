package dev.lumyrix.lumlib.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.lumyrix.lumlib.api.LumLib;
import dev.lumyrix.lumlib.gui.LumModListScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LumLib's own ModMenu entry opens the mod-list screen. Additionally, every mod whose
 * fabric.mod.json has {@code "custom": {"lumlib": true}} gets its own ModMenu config button
 * (resolved lazily at click time, so it does not depend on initialization order).
 */
public final class LumLibModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LumModListScreen::new;
    }

    @Override
    public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
        Map<String, ConfigScreenFactory<?>> map = new LinkedHashMap<>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            String id = mod.getMetadata().getId();
            if (id.equals(LumLib.MOD_ID) || !mod.getMetadata().containsCustomValue("lumlib")) {
                continue;
            }
            map.put(id, parent -> LumModMenuScreenFactory.forMod(id).apply(parent));
        }
        return map;
    }
}
