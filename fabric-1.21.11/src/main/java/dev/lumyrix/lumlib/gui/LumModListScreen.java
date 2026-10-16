package dev.lumyrix.lumlib.gui;

import dev.lumyrix.lumlib.api.LumConfigContainer;
import dev.lumyrix.lumlib.config.LumConfigRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Fallback entry point when ModMenu is not installed: lists every mod that
 * has registered with LumLib and opens that mod's {@link LumConfigScreen} on
 * click. If a mod registered but added zero options, its row is disabled and
 * shows a short "no settings" note instead of navigating anywhere.
 */
public final class LumModListScreen extends Screen {

    private static final int ROW_WIDTH = 280;
    private static final int ROW_HEIGHT = 22;
    private static final int TOP_MARGIN = 34;
    private static final int BOTTOM_MARGIN = 30;

    private final Screen parent;
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private final List<AbstractWidget> modRows = new ArrayList<>();

    public LumModListScreen(Screen parent) {
        super(Component.translatable("lumlib.modlist.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        modRows.clear();
        Collection<LumConfigContainer> mods = LumConfigRegistry.all();

        int cx = this.width / 2;
        int left = cx - ROW_WIDTH / 2;
        int y = TOP_MARGIN;

        for (LumConfigContainer container : mods) {
            boolean hasOptions = !container.allOptions().isEmpty();
            ModRowButton row = new ModRowButton(left, y, ROW_WIDTH, ROW_HEIGHT, container, hasOptions, b -> {
                if (hasOptions) {
                    this.minecraft.setScreen(new LumConfigScreen(this, container));
                }
            });
            row.active = hasOptions;
            addRenderableWidget(row);
            modRows.add(row);
            y += ROW_HEIGHT + 2;
        }

        int contentHeight = y - TOP_MARGIN;
        int visibleHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        relayout();

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), b -> onClose())
                .bounds(cx - 50, this.height - 26, 100, 20)
                .build());
    }

    private void relayout() {
        int cx = this.width / 2;
        int left = cx - ROW_WIDTH / 2;
        int y = TOP_MARGIN - scrollOffset;
        int visibleTop = TOP_MARGIN - 4;
        int visibleBottom = this.height - BOTTOM_MARGIN;
        for (AbstractWidget row : modRows) {
            row.setY(y);
            row.setX(left);
            // Rows scrolled behind the title/footer are hidden outright so they
            // neither draw over those bars nor intercept clicks meant for them.
            row.visible = y >= visibleTop && y + ROW_HEIGHT <= visibleBottom;
            y += ROW_HEIGHT + 2;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * 16)));
            relayout();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.render(gfx, mouseX, mouseY, delta);

        gfx.drawCenteredString(font, getTitle(), this.width / 2, 12, LumTheme.TEXT_TITLE);

        if (modRows.isEmpty()) {
            gfx.drawCenteredString(font, Component.translatable("lumlib.modlist.empty"),
                    this.width / 2, this.height / 2, LumTheme.TEXT_SECONDARY);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    /**
     * A single row: mod display name on the left, a "no settings" hint on the
     * right when the mod registered but has nothing to configure.
     */
    private final class ModRowButton extends AbstractWidget {
        private final LumConfigContainer container;
        private final boolean hasOptions;
        private final java.util.function.Consumer<ModRowButton> onPress;

        ModRowButton(int x, int y, int width, int height, LumConfigContainer container, boolean hasOptions,
                     java.util.function.Consumer<ModRowButton> onPress) {
            super(x, y, width, height, Component.literal(container.displayName()));
            this.container = container;
            this.hasOptions = hasOptions;
            this.onPress = onPress;
        }

        @Override
        public void onClick(net.minecraft.client.input.MouseButtonEvent event, boolean isDoubleClick) {
            onPress.accept(this);
        }

        @Override
        protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
            boolean hovered = isHoveredOrFocused();
            int bg = hovered && hasOptions ? LumTheme.ROW_HOVER : 0x00000000;
            if (bg != 0) {
                gfx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bg);
            }
            dev.lumyrix.lumlib.gui.LumThemeLegacy.drawOutline(gfx, getX(), getY(), getWidth(), getHeight(), LumTheme.PANEL_BORDER);

            int textColor = hasOptions ? LumTheme.TEXT_PRIMARY : LumTheme.TEXT_DISABLED;
            gfx.drawString(font, container.displayName(), getX() + 8, getY() + (getHeight() - 8) / 2, textColor, false);

            if (!hasOptions) {
                Component note = Component.translatable("lumlib.modlist.nosettings");
                int noteWidth = font.width(note);
                gfx.drawString(font, note, getX() + getWidth() - noteWidth - 8,
                        getY() + (getHeight() - 8) / 2, LumTheme.TEXT_SECONDARY, false);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            output.add(NarratedElementType.TITLE, getMessage());
        }
    }
}
