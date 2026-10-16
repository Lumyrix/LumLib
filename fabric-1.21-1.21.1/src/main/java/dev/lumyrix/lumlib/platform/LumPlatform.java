package dev.lumyrix.lumlib.platform;

import dev.lumyrix.lumlib.gui.LumModListScreen;
import dev.lumyrix.lumlib.gui.LumTheme;
import dev.lumyrix.lumlib.gui.widget.LumGearButton;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.network.chat.Component;

import java.time.Duration;

/**
 * Adds LumLib's small gear button to the pause screen's bottom-right corner,
 * but only when ModMenu is not installed — if it is, ModMenu already exposes
 * every registered mod's config individually and a second entry point would
 * be redundant clutter. Uses Fabric's screen events instead of a mixin, so it
 * does not depend on PauseScreen's internal method names.
 */
public final class LumPlatform {

    private LumPlatform() {
    }

    public static void init() {
        if (FabricLoader.getInstance().isModLoaded("modmenu")) {
            return;
        }
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!(screen instanceof PauseScreen)) {
                return;
            }
            int size = LumTheme.GEAR_BUTTON_SIZE;
            int margin = LumTheme.GEAR_BUTTON_MARGIN;
            int x = scaledWidth - size - margin;
            int y = scaledHeight - size - margin;

            Button gear = LumGearButton.create(x, y, () -> client.setScreen(new LumModListScreen(screen)));
            gear.setTooltip(Tooltip.create(Component.translatable("lumlib.button.configure.tooltip")));
            gear.setTooltipDelay(Duration.ofMillis(250));
            Screens.getButtons(screen).add(gear);

            ScreenEvents.afterRender(screen).register((s, graphics, mouseX, mouseY, delta) ->
                    LumGearButton.drawOverlay(graphics, gear));
        });
    }
}
