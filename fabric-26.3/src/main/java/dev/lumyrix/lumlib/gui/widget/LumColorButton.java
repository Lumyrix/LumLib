package dev.lumyrix.lumlib.gui.widget;

import dev.lumyrix.lumlib.gui.LumTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.IntSupplier;

/**
 * Factory for a color-swatch button. Built through {@link Button#builder}
 * (the same construction path vanilla and every LumLib screen already uses)
 * rather than subclassing {@code Button} directly, since the exact
 * constructor signature for direct subclassing has shifted across Minecraft
 * versions. The swatch preview and hex label are drawn as a small overlay by
 * {@link #drawOverlay} immediately after the button itself renders.
 */
public final class LumColorButton {

    private LumColorButton() {
    }

    public static Button create(int x, int y, int width, int height, Runnable onPress) {
        return Button.builder(Component.empty(), b -> onPress.run())
                .bounds(x, y, width, height)
                .build();
    }

    /**
     * Call once per frame, right after the button itself has rendered, to draw
     * the color preview (with checkerboard for alpha) and hex label on top of it.
     */
    public static void drawOverlay(GuiGraphicsExtractor gfx, Button button, IntSupplier colorSupplier) {
        int x = button.getX();
        int y = button.getY();
        int h = button.getHeight();
        int argb = colorSupplier.getAsInt();

        drawCheckerboard(gfx, x + 3, y + 3, 18, h - 6);
        gfx.fill(x + 3, y + 3, x + 21, y + h - 3, argb);
        gfx.outline(x + 3, y + 3, 18, h - 6, 0x60000000);

        String hex = String.format(java.util.Locale.ROOT, "#%08X", argb);
        int textColor = button.active ? LumTheme.TEXT_PRIMARY : LumTheme.TEXT_DISABLED;
        gfx.text(Minecraft.getInstance().font, hex, x + 27, y + (h - 8) / 2, textColor, false);
    }

    private static void drawCheckerboard(GuiGraphicsExtractor gfx, int x, int y, int w, int h) {
        int cell = 4;
        for (int cy = 0; cy < h; cy += cell) {
            for (int cx = 0; cx < w; cx += cell) {
                boolean light = ((cx / cell) + (cy / cell)) % 2 == 0;
                int color = light ? 0xFFCCCCCC : 0xFF888888;
                int drawW = Math.min(cell, w - cx);
                int drawH = Math.min(cell, h - cy);
                gfx.fill(x + cx, y + cy, x + cx + drawW, y + cy + drawH, color);
            }
        }
    }
}
