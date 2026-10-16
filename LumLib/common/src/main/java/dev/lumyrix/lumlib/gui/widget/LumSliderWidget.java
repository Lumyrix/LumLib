package dev.lumyrix.lumlib.gui.widget;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

/**
 * A generic numeric slider used for both {@code INT_SLIDER} and
 * {@code DOUBLE_SLIDER} option types. Internally always works in normalized
 * 0..1 space (matching vanilla's {@link AbstractSliderButton}) and converts
 * to/from the real value range at the edges.
 */
public final class LumSliderWidget extends AbstractSliderButton {

    private final double min;
    private final double max;
    private final double step;
    private Function<Double, Component> formatter;
    private Consumer<Double> onChange;

    public LumSliderWidget(int x, int y, int width, int height, double min, double max, double step,
                            double initial, Function<Double, Component> formatter, Consumer<Double> onChange) {
        // AbstractSliderButton's own constructor invokes updateMessage() before this
        // subclass's fields are assigned, so 'formatter' is briefly null during
        // super(...). updateMessage() below guards against that.
        super(x, y, width, height, Component.empty(), normalize(initial, min, max));
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? (max - min) / 100.0 : step;
        this.formatter = formatter;
        this.onChange = onChange;
        updateMessage();
    }

    private static double normalize(double value, double min, double max) {
        if (max <= min) return 0.0;
        return clamp((value - min) / (max - min), 0.0, 1.0);
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /**
     * Programmatically moves the slider to a real (denormalized) value and
     * refreshes its label, without firing {@link #applyValue()}. Used to keep
     * this slider in sync when its value changed through another input (e.g.
     * a hex field) rather than a drag.
     */
    public void setValueSilently(double realValue) {
        this.value = normalize(realValue, min, max);
        updateMessage();
    }

    /** Current real (denormalized, step-snapped) value. */
    public double currentValue() {
        double raw = min + (max - min) * this.value;
        double snapped = Math.round(raw / step) * step;
        return clamp(snapped, min, max);
    }

    @Override
    protected void updateMessage() {
        if (formatter == null) return; // called once by the superclass constructor before our fields exist
        this.setMessage(formatter.apply(currentValue()));
    }

    @Override
    protected void applyValue() {
        onChange.accept(currentValue());
    }

    /** Convenience factory for integer sliders with a plain "%d" style formatter. */
    public static LumSliderWidget forInt(int x, int y, int width, int height,
                                          int min, int max, int step, int initial,
                                          Consumer<Integer> onChange) {
        return new LumSliderWidget(x, y, width, height, min, max, Math.max(1, step), initial,
                v -> Component.literal(String.valueOf(Math.round(v))),
                v -> onChange.accept((int) Math.round(v)));
    }

    /** Convenience factory for double sliders formatted to 2 decimal places. */
    public static LumSliderWidget forDouble(int x, int y, int width, int height,
                                             double min, double max, double step, double initial,
                                             Consumer<Double> onChange) {
        DoubleUnaryOperator round2 = v -> Math.round(v * 100.0) / 100.0;
        return new LumSliderWidget(x, y, width, height, min, max, step, initial,
                v -> Component.literal(String.format(java.util.Locale.ROOT, "%.2f", round2.applyAsDouble(v))),
                onChange);
    }
}
