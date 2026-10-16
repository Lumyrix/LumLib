package dev.lumyrix.lumlib.gui.widget;

import dev.lumyrix.lumlib.gui.LumTheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * The small square gear-icon button LumLib adds to the bottom-right corner
 * of the pause screen when ModMenu isn't installed. The gear glyph is drawn
 * procedurally (a ring of short spokes around a hollow center) instead of
 * shipping a texture, so there's no extra asset to load or go missing across
 * resource-pack overrides.
 */
public final class LumGearButton {

    private LumGearButton() {
    }

    public static Button create(int x, int y, Runnable onPress) {
        int size = LumTheme.GEAR_BUTTON_SIZE;
        return Button.builder(Component.translatable("lumlib.button.configure"), b -> onPress.run())
                .bounds(x, y, size, size)
                .build();
    }

    /** Call right after the button renders, to draw the flat background + gear glyph on top. */
    public static void drawOverlay(GuiGraphics gfx, Button button) {
        int x = button.getX();
        int y = button.getY();
        int size = button.getWidth();
        boolean hovered = button.isHoveredOrFocused();

        int bg = hovered ? LumTheme.GEAR_BUTTON_BG_HOVER : LumTheme.GEAR_BUTTON_BG;
        gfx.fill(x, y, x + size, y + size, bg);
        LumTheme.drawOutline(gfx, x, y, size, size, 0x50000000);

        drawGear(gfx, x + size / 2, y + size / 2, size);
    }

    private static void drawGear(GuiGraphics gfx, int cx, int cy, int size) {
        int color = LumTheme.GEAR_ICON_COLOR;
        int outerR = size / 2 - 3;
        int innerR = Math.max(2, outerR - 3);
        int hubR = Math.max(1, outerR / 3);

        // Eight short spokes radiating from the hub, each drawn as a tiny filled
        // rect rotated by hand-picked offsets — cheap, no trig-heavy per-frame cost
        // since these offsets are constants baked at compile time.
        int[][] spokeOffsets = {
                {0, -1}, {1, -1}, {1, 0}, {1, 1},
                {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}
        };
        for (int[] dir : spokeOffsets) {
            int sx = cx + dir[0] * innerR;
            int sy = cy + dir[1] * innerR;
            int ex = cx + dir[0] * outerR;
            int ey = cy + dir[1] * outerR;
            gfx.fill(Math.min(sx, ex) - 1, Math.min(sy, ey) - 1, Math.max(sx, ex) + 1, Math.max(sy, ey) + 1, color);
        }

        // Hollow hub: filled circle-ish square with a punched-out center square,
        // giving the classic gear "ring" silhouette at tiny sizes.
        fillSquare(gfx, cx, cy, hubR + 2, color);
        fillSquare(gfx, cx, cy, Math.max(1, hubR - 1), LumTheme.GEAR_BUTTON_BG);
    }

    private static void fillSquare(GuiGraphics gfx, int cx, int cy, int r, int color) {
        gfx.fill(cx - r, cy - r, cx + r, cy + r, color);
    }
}
