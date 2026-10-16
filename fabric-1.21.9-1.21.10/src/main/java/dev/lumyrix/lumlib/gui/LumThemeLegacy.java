package dev.lumyrix.lumlib.gui;

import net.minecraft.client.gui.GuiGraphics;

public final class LumThemeLegacy {
    private LumThemeLegacy() {
    }

    public static void drawOutline(
            GuiGraphics gfx, int x, int y, int width, int height, int color) {
        if (width <= 0 || height <= 0) return;

        gfx.fill(x, y, x + width, y + 1, color);

        if (height > 1) {
            gfx.fill(x, y + height - 1, x + width, y + height, color);
        }

        if (height > 2) {
            gfx.fill(x, y + 1, x + 1, y + height - 1, color);

            if (width > 1) {
                gfx.fill(x + width - 1, y + 1,
                         x + width, y + height - 1, color);
            }
        }
    }
}
