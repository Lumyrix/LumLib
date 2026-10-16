package dev.lumyrix.lumlib.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A single, generically-typed config option.
 * <p>
 * Instances are built exclusively through {@link LumConfigContainer}'s
 * {@code addXxx} methods — there is no public constructor.
 *
 * @param <T> the value type this option holds (Boolean, Integer, Double, String,
 *            an enum type, or Integer for ARGB colors)
 */
public final class LumOption<T> {

    private final String key;
    private final String label;
    private final String tooltip;
    private final LumOptionType type;
    private final T defaultValue;
    private final Supplier<T> getter;
    private final Consumer<T> setter;

    // Numeric bounds (sliders only)
    private final Number min;
    private final Number max;
    private final Number step;

    // Enum choices (enum widget only) — stored as display-string list plus raw enum constants
    private final List<? extends Enum<?>> enumValues;

    // Text constraints (text widget only)
    private final int maxLength;

    // Color: whether alpha channel is editable
    private final boolean allowAlpha;

    private LumOption(Builder<T> b) {
        this.key = b.key;
        this.label = b.label;
        this.tooltip = b.tooltip;
        this.type = b.type;
        this.defaultValue = b.defaultValue;
        this.getter = b.getter;
        this.setter = b.setter;
        this.min = b.min;
        this.max = b.max;
        this.step = b.step;
        this.enumValues = b.enumValues;
        this.maxLength = b.maxLength;
        this.allowAlpha = b.allowAlpha;
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public String tooltip() {
        return tooltip;
    }

    public LumOptionType type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public T get() {
        return getter.get();
    }

    @SuppressWarnings("unchecked")
    public void setRaw(Object value) {
        setter.accept((T) value);
    }

    public void set(T value) {
        setter.accept(value);
    }

    public Number min() {
        return min;
    }

    public Number max() {
        return max;
    }

    public Number step() {
        return step;
    }

    public List<? extends Enum<?>> enumValues() {
        return enumValues;
    }

    public int maxLength() {
        return maxLength;
    }

    public boolean allowAlpha() {
        return allowAlpha;
    }

    static <T> Builder<T> builder(LumOptionType type, String key, String label, T defaultValue,
                                   Supplier<T> getter, Consumer<T> setter) {
        return new Builder<>(type, key, label, defaultValue, getter, setter);
    }

    static final class Builder<T> {
        private final String key;
        private final String label;
        private final LumOptionType type;
        private final T defaultValue;
        private final Supplier<T> getter;
        private final Consumer<T> setter;
        private String tooltip = "";
        private Number min;
        private Number max;
        private Number step;
        private List<? extends Enum<?>> enumValues;
        private int maxLength = 256;
        private boolean allowAlpha = true;

        private Builder(LumOptionType type, String key, String label, T defaultValue,
                         Supplier<T> getter, Consumer<T> setter) {
            this.type = type;
            this.key = key;
            this.label = label;
            this.defaultValue = defaultValue;
            this.getter = getter;
            this.setter = setter;
        }

        Builder<T> tooltip(String tooltip) {
            this.tooltip = tooltip == null ? "" : tooltip;
            return this;
        }

        Builder<T> range(Number min, Number max, Number step) {
            this.min = min;
            this.max = max;
            this.step = step;
            return this;
        }

        Builder<T> enumValues(List<? extends Enum<?>> values) {
            this.enumValues = values;
            return this;
        }

        Builder<T> maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        Builder<T> allowAlpha(boolean allowAlpha) {
            this.allowAlpha = allowAlpha;
            return this;
        }

        LumOption<T> build() {
            return new LumOption<>(this);
        }
    }
}
