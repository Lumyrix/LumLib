package dev.lumyrix.lumlib.gui;

import dev.lumyrix.lumlib.gui.widget.LumSliderWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

/**
 * Full-screen color editor: a hex text field, four channel sliders (R/G/B/A —
 * the alpha slider is hidden entirely when the option disallows alpha), and a
 * large live preview swatch over a checkerboard so translucency is visible.
 * <p>
 * The hex field and the sliders stay in sync in both directions: typing a
 * valid hex code moves the sliders, dragging a slider updates the hex field.
 */
public final class LumColorPickerScreen extends Screen {

    private final Screen parent;
    private final boolean allowAlpha;
    private final IntConsumer onConfirm;

    private int a, r, g, b;
    private EditBox hexField;
    private LumSliderWidget rSlider, gSlider, bSlider, aSlider;
    private boolean syncingFromSliders = false;

    public LumColorPickerScreen(Screen parent, Component title, int initialArgb, boolean allowAlpha,
                                 IntConsumer onConfirm) {
        super(title);
        this.parent = parent;
        this.allowAlpha = allowAlpha;
        this.onConfirm = onConfirm;
        unpack(initialArgb);
    }

    private void unpack(int argb) {
        a = (argb >>> 24) & 0xFF;
        r = (argb >>> 16) & 0xFF;
        g = (argb >>> 8) & 0xFF;
        b = argb & 0xFF;
        if (!allowAlpha) a = 0xFF;
    }

    private int packed() {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int panelW = 260;
        int left = cx - panelW / 2;
        int y = this.height / 2 - 90;

        // Hex field
        hexField = new EditBox(this.font, left, y, panelW, 20,
                Component.translatable("lumlib.colorpicker.hex"));
        hexField.setMaxLength(allowAlpha ? 9 : 7);
        hexField.setValue(formatHex());
        hexField.setResponder(this::onHexTyped);
        addRenderableWidget(hexField);
        y += 30;

        rSlider = LumSliderWidget.forInt(left, y, panelW, 20, 0, 255, 1, r, v -> {
            r = v;
            syncFromSliders();
        });
        addRenderableWidget(rSlider);
        y += 24;

        gSlider = LumSliderWidget.forInt(left, y, panelW, 20, 0, 255, 1, g, v -> {
            g = v;
            syncFromSliders();
        });
        addRenderableWidget(gSlider);
        y += 24;

        bSlider = LumSliderWidget.forInt(left, y, panelW, 20, 0, 255, 1, b, v -> {
            b = v;
            syncFromSliders();
        });
        addRenderableWidget(bSlider);
        y += 24;

        if (allowAlpha) {
            aSlider = LumSliderWidget.forInt(left, y, panelW, 20, 0, 255, 1, a, v -> {
                a = v;
                syncFromSliders();
            });
            addRenderableWidget(aSlider);
            y += 24;
        }

        y += 16;
        int btnW = (panelW - 8) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> {
                    onConfirm.accept(packed());
                    this.minecraft.setScreenAndShow(parent);
                })
                .bounds(left, y, btnW, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> this.minecraft.setScreenAndShow(parent))
                .bounds(left + btnW + 8, y, btnW, 20).build());
    }

    private String formatHex() {
        return allowAlpha
                ? String.format(java.util.Locale.ROOT, "%02X%02X%02X%02X", a, r, g, b)
                : String.format(java.util.Locale.ROOT, "%02X%02X%02X", r, g, b);
    }

    private void onHexTyped(String raw) {
        if (syncingFromSliders) return;
        String hex = raw.startsWith("#") ? raw.substring(1) : raw;
        try {
            if (allowAlpha && hex.length() == 8) {
                long v = Long.parseLong(hex, 16);
                a = (int) ((v >>> 24) & 0xFF);
                r = (int) ((v >>> 16) & 0xFF);
                g = (int) ((v >>> 8) & 0xFF);
                b = (int) (v & 0xFF);
            } else if (hex.length() == 6) {
                int v = Integer.parseInt(hex, 16);
                r = (v >>> 16) & 0xFF;
                g = (v >>> 8) & 0xFF;
                b = v & 0xFF;
                if (!allowAlpha) a = 0xFF;
            } else {
                return; // incomplete input, wait for more characters
            }
        } catch (NumberFormatException ignored) {
            return;
        }
        pushToSliders();
    }

    private void syncFromSliders() {
        syncingFromSliders = true;
        hexField.setValue(formatHex());
        syncingFromSliders = false;
    }

    private void pushToSliders() {
        rSlider.setValueSilently(r);
        gSlider.setValueSilently(g);
        bSlider.setValueSilently(b);
        if (aSlider != null) aSlider.setValueSilently(a);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        // Screen.render draws the background and the widgets; everything custom goes on top.
        super.extractRenderState(gfx, mouseX, mouseY, delta);

        int cx = this.width / 2;
        gfx.centeredText(font, getTitle(), cx, this.height / 2 - 118, LumTheme.TEXT_TITLE);

        // Large preview swatch to the right of the panel
        int previewSize = 48;
        int previewX = cx + 260 / 2 + 16;
        int previewY = this.height / 2 - 90;
        drawCheckerboard(gfx, previewX, previewY, previewSize, previewSize);
        gfx.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, packed());
        gfx.outline(previewX, previewY, previewSize, previewSize, 0x80000000);

        // Channel labels drawn just left of each slider row
        int left = cx - 260 / 2;
        drawRowLabel(gfx, "R", left, rSlider.getY());
        drawRowLabel(gfx, "G", left, gSlider.getY());
        drawRowLabel(gfx, "B", left, bSlider.getY());
        if (aSlider != null) drawRowLabel(gfx, "A", left, aSlider.getY());
    }

    private void drawRowLabel(GuiGraphicsExtractor gfx, String letter, int left, int rowY) {
        gfx.text(font, letter, left - 12, rowY + 6, LumTheme.TEXT_SECONDARY, false);
    }

    private static void drawCheckerboard(GuiGraphicsExtractor gfx, int x, int y, int w, int h) {
        int cell = 6;
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

    @Override
    public boolean shouldCloseOnEsc() {
        return true; // Esc behaves like Cancel: returns to the parent screen without saving
    }

    @Override
    public void onClose() {
        this.minecraft.setScreenAndShow(parent);
    }
}
