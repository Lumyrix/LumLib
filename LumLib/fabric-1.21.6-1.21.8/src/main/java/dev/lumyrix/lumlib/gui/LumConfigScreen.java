package dev.lumyrix.lumlib.gui;

import dev.lumyrix.lumlib.api.LumConfigContainer;
import dev.lumyrix.lumlib.api.LumOption;
import dev.lumyrix.lumlib.api.LumOptionGroup;
import dev.lumyrix.lumlib.config.LumConfigStorage;
import dev.lumyrix.lumlib.gui.widget.LumColorButton;
import dev.lumyrix.lumlib.gui.widget.LumSliderWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

/**
 * Renders every option registered on a {@link LumConfigContainer} as a
 * scrollable list of grouped widgets. Widget construction is entirely
 * data-driven off each {@link LumOption}'s {@link dev.lumyrix.lumlib.api.LumOptionType} —
 * mods never write GUI code themselves.
 */
public final class LumConfigScreen extends Screen {

    private static final int CONTENT_WIDTH = 280;
    private static final int TOP_MARGIN = 34;
    private static final int BOTTOM_MARGIN = 30;

    private final Screen parent;
    private final LumConfigContainer container;

    private int scrollOffset = 0;
    private int maxScroll = 0;
    private final List<Row> rows = new ArrayList<>();
    // Color buttons need their swatch redrawn on top every frame, keyed by the option they represent.
    private final Map<Button, IntSupplier> colorOverlays = new LinkedHashMap<>();

    public LumConfigScreen(Screen parent, LumConfigContainer container) {
        super(Component.literal(container.displayName()));
        this.parent = parent;
        this.container = container;
    }

    private record Row(String headerOrNull, AbstractWidget widget) {
        static Row header(String title) {
            return new Row(title, null);
        }

        static Row widget(AbstractWidget widget) {
            return new Row(null, widget);
        }

        boolean isHeader() {
            return headerOrNull != null;
        }
    }

    @Override
    protected void init() {
        rows.clear();
        colorOverlays.clear();

        if (!container.ungrouped().isEmpty()) {
            for (LumOption<?> option : container.ungrouped()) {
                rows.add(Row.widget(buildWidget(option)));
            }
        }
        for (LumOptionGroup group : container.groups()) {
            rows.add(Row.header(group.title()));
            for (LumOption<?> option : group.options()) {
                rows.add(Row.widget(buildWidget(option)));
            }
        }

        layoutRows();

        int buttonY = this.height - 26;
        int buttonLeft = this.width / 2 - 132;
        addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
                    for (LumOption<?> option : container.allOptions()) option.setRaw(option.defaultValue());
                    this.minecraft.setScreenAndShow(new LumConfigScreen(this.parent, this.container));
                })
                .bounds(buttonLeft, buttonY, 84, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> LumConfigStorage.save(container))
                .bounds(buttonLeft + 90, buttonY, 84, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("OK"), b -> onClose())
                .bounds(buttonLeft + 180, buttonY, 84, 20)
                .build());
    }

    private void layoutRows() {
        int cx = this.width / 2;
        int left = cx - CONTENT_WIDTH / 2;
        int y = TOP_MARGIN - scrollOffset;
        int visibleTop = TOP_MARGIN - 4;
        int visibleBottom = this.height - BOTTOM_MARGIN;

        for (Row row : rows) {
            if (row.isHeader()) {
                y += LumTheme.GROUP_HEADER_HEIGHT;
            } else {
                AbstractWidget w = row.widget();
                w.setX(left);
                w.setY(y);
                if (!(w instanceof Checkbox)) {
                    // Checkbox sizes itself to its label text at build time; forcing a
                    // uniform width would stretch its click area without stretching
                    // what's actually drawn, so every other widget type gets the
                    // shared column width but Checkbox keeps its own.
                    w.setWidth(CONTENT_WIDTH);
                }
                // The content region is clipped with a scissor in render(), but a
                // scissor only affects drawing, not input — without this, a widget
                // scrolled behind the title or footer bar would still eat clicks
                // meant for whatever's visibly on top of it. visible=false skips
                // both rendering and hit-testing for AbstractWidget.
                boolean inView = y >= visibleTop && y + LumTheme.ROW_HEIGHT <= visibleBottom;
                w.visible = inView;
                y += LumTheme.ROW_HEIGHT;
            }
        }

        int contentHeight = y - (TOP_MARGIN - scrollOffset);
        int visibleHeight = this.height - TOP_MARGIN - BOTTOM_MARGIN;
        maxScroll = Math.max(0, contentHeight - visibleHeight);
        if (scrollOffset > maxScroll) scrollOffset = maxScroll;
    }

    private AbstractWidget buildWidget(LumOption<?> option) {
        AbstractWidget widget = switch (option.type()) {
            case BOOLEAN -> buildBoolean(option);
            case INT_SLIDER -> buildIntSlider(option);
            case DOUBLE_SLIDER -> buildDoubleSlider(option);
            case TEXT -> buildText(option);
            case ENUM -> buildEnum(option);
            case COLOR -> buildColor(option);
        };
        addRenderableWidget(widget);
        return widget;
    }

    @SuppressWarnings("unchecked")
    private AbstractWidget buildBoolean(LumOption<?> option) {
        LumOption<Boolean> opt = (LumOption<Boolean>) option;
        Checkbox box = Checkbox.builder(Component.literal(opt.label()), this.font)
                .selected(opt.get())
                .onValueChange((cb, v) -> opt.set(v))
                .build();
        if (!opt.tooltip().isEmpty()) {
            box.setTooltip(Tooltip.create(Component.literal(opt.tooltip())));
            box.setTooltipDelay(Duration.ofMillis(250));
        }
        return box;
    }

    @SuppressWarnings("unchecked")
    private AbstractWidget buildIntSlider(LumOption<?> option) {
        LumOption<Integer> opt = (LumOption<Integer>) option;
        int min = opt.min().intValue();
        int max = opt.max().intValue();
        int step = opt.step() == null ? 1 : opt.step().intValue();
        LumSliderWidget slider = LumSliderWidget.forInt(0, 0, CONTENT_WIDTH, 20, min, max, step, opt.get(), opt::set);
        applyLabelPrefixedTooltip(slider, opt);
        return slider;
    }

    @SuppressWarnings("unchecked")
    private AbstractWidget buildDoubleSlider(LumOption<?> option) {
        LumOption<Double> opt = (LumOption<Double>) option;
        double min = opt.min().doubleValue();
        double max = opt.max().doubleValue();
        double step = opt.step() == null ? (max - min) / 100.0 : opt.step().doubleValue();
        LumSliderWidget slider = LumSliderWidget.forDouble(0, 0, CONTENT_WIDTH, 20, min, max, step, opt.get(), opt::set);
        applyLabelPrefixedTooltip(slider, opt);
        return slider;
    }

    @SuppressWarnings("unchecked")
    private AbstractWidget buildText(LumOption<?> option) {
        LumOption<String> opt = (LumOption<String>) option;
        EditBox box = new EditBox(this.font, 0, 0, CONTENT_WIDTH, 20, Component.literal(opt.label()));
        box.setMaxLength(opt.maxLength());
        box.setValue(opt.get());
        box.setResponder(opt::set);
        if (!opt.tooltip().isEmpty()) {
            box.setTooltip(Tooltip.create(Component.literal(opt.tooltip())));
        }
        return box;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private AbstractWidget buildEnum(LumOption<?> option) {
        LumOption<Enum> opt = (LumOption<Enum>) option;
        List<? extends Enum<?>> values = opt.enumValues();
        Button btn = Button.builder(enumLabel(opt, opt.get()), b -> {
                    int idx = values.indexOf(opt.get());
                    Enum<?> next = values.get((idx + 1) % values.size());
                    opt.set(next);
                    b.setMessage(enumLabel(opt, next));
                })
                .bounds(0, 0, CONTENT_WIDTH, 20)
                .build();
        if (!opt.tooltip().isEmpty()) {
            btn.setTooltip(Tooltip.create(Component.literal(opt.tooltip())));
            btn.setTooltipDelay(Duration.ofMillis(250));
        }
        return btn;
    }

    private Component enumLabel(LumOption<?> opt, Enum<?> value) {
        return Component.literal(opt.label() + ": " + prettify(value.name()));
    }

    private static String prettify(String enumName) {
        String[] parts = enumName.toLowerCase(java.util.Locale.ROOT).split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private AbstractWidget buildColor(LumOption<?> option) {
        LumOption<Integer> opt = (LumOption<Integer>) option;
        Button btn = LumColorButton.create(0, 0, CONTENT_WIDTH, 20, () ->
                this.minecraft.setScreen(new LumColorPickerScreen(
                        this, Component.literal(opt.label()), opt.get(), opt.allowAlpha(), opt::set)));
        if (!opt.tooltip().isEmpty()) {
            btn.setTooltip(Tooltip.create(Component.literal(opt.tooltip())));
            btn.setTooltipDelay(Duration.ofMillis(250));
        }
        colorOverlays.put(btn, opt::get);
        return btn;
    }

    private void applyLabelPrefixedTooltip(LumSliderWidget slider, LumOption<?> opt) {
        if (!opt.tooltip().isEmpty()) {
            slider.setTooltip(Tooltip.create(Component.literal(opt.label() + " — " + opt.tooltip())));
            slider.setTooltipDelay(Duration.ofMillis(250));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (maxScroll > 0) {
            scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) (scrollY * 16)));
            layoutRows();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        // Screen.render already draws the background and every widget; drawing the
        // background here as well would blur twice per frame and crash on 1.21.6+.
        super.render(gfx, mouseX, mouseY, delta);

        int cx = this.width / 2;
        int left = cx - CONTENT_WIDTH / 2;
        int visibleTop = TOP_MARGIN - 4;
        int visibleBottom = this.height - BOTTOM_MARGIN;
        int y = TOP_MARGIN - scrollOffset;
        for (Row row : rows) {
            if (row.isHeader()) {
                if (y >= visibleTop && y + LumTheme.GROUP_HEADER_HEIGHT <= visibleBottom) {
                    gfx.drawString(font, row.headerOrNull(), left, y + 6, LumTheme.ACCENT, false);
                }
                y += LumTheme.GROUP_HEADER_HEIGHT;
            } else {
                y += LumTheme.ROW_HEIGHT;
            }
        }

        for (Map.Entry<Button, IntSupplier> entry : colorOverlays.entrySet()) {
            if (entry.getKey().visible) {
                LumColorButton.drawOverlay(gfx, entry.getKey(), entry.getValue());
            }
        }

        gfx.drawCenteredString(font, getTitle(), cx, 12, LumTheme.TEXT_TITLE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        LumConfigStorage.save(container);
        this.minecraft.setScreen(parent);
    }
}
