package dev.lumyrix.lumlib.gui;


/**
 * Shared color/spacing constants for every LumLib-drawn screen and widget,
 * so all generated config screens look consistent regardless of which mod
 * registered the options.
 */
public final class LumTheme {

    private LumTheme() {
    }

    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFFAAAAAA;
    public static final int TEXT_DISABLED = 0xFF6B6B6B;
    public static final int TEXT_TITLE = 0xFFFFFFFF;

    public static final int ACCENT = 0xFF5C9DF5;
    public static final int ACCENT_HOVER = 0xFF7CB0F7;

    public static final int PANEL_BG = 0xB0101014;
    public static final int PANEL_BORDER = 0x40FFFFFF;

    public static final int ROW_HOVER = 0x30FFFFFF;
    public static final int ROW_HEIGHT = 24;
    public static final int GROUP_HEADER_HEIGHT = 20;

    public static final int GEAR_BUTTON_SIZE = 20;
    public static final int GEAR_BUTTON_MARGIN = 6;
    public static final int GEAR_ICON_COLOR = 0xFF1A1A1A;
    public static final int GEAR_BUTTON_BG = 0xE0DADADA;
    public static final int GEAR_BUTTON_BG_HOVER = 0xE0F0F0F0;

}
