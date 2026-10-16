package dev.lumyrix.lumlib;

import dev.lumyrix.lumlib.platform.LumPlatform;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LumLib's client entrypoint. There is intentionally very little to do here:
 * the library is passive infrastructure — mods call {@code LumLib.registerMod(...)}
 * from their own entrypoints whenever they're ready, and the pause-screen
 * gear button (added by LumPlatform) checks the registry lazily each time the
 * pause screen opens.
 */
public final class LumLibClientEntrypoint implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("lumlib");

    @Override
    public void onInitializeClient() {
        LumPlatform.init();
        LOGGER.info("[LumLib] initialized");
    }
}
