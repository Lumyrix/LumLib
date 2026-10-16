package dev.lumyrix.lumlib.api;

import dev.lumyrix.lumlib.config.LumConfigRegistry;
import dev.lumyrix.lumlib.config.LumConfigStorage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Fluent builder a mod uses to describe its configuration to LumLib.
 * <p>
 * Obtain one via {@link LumLib#registerMod(String, String)}, add options
 * (optionally organized into groups with {@link #group(String)}), then call
 * {@link #build()} exactly once. After {@code build()}, LumLib owns loading
 * the saved JSON (if any exists) and applying it to your setters immediately.
 */
public final class LumConfigContainer {

    private final String modId;
    private final String displayName;
    private final Map<String, LumOptionGroup> groups = new LinkedHashMap<>();
    private final List<LumOption<?>> ungrouped = new ArrayList<>();
    private LumOptionGroup currentGroup;
    private boolean built = false;

    LumConfigContainer(String modId, String displayName) {
        this.modId = modId;
        this.displayName = displayName;
    }

    public String modId() {
        return modId;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Starts (or resumes) a named group. Every {@code addXxx} call after this
     * belongs to this group until another {@code group(...)} call is made.
     * Calling this is optional — ungrouped options render under an unlabeled section first.
     */
    public LumConfigContainer group(String title) {
        currentGroup = groups.computeIfAbsent(title, LumOptionGroup::new);
        return this;
    }

    public LumConfigContainer addBoolean(String key, String label, boolean def,
                                          Consumer<Boolean> setter, Supplier<Boolean> getter) {
        return addBoolean(key, label, "", def, setter, getter);
    }

    public LumConfigContainer addBoolean(String key, String label, String tooltip, boolean def,
                                          Consumer<Boolean> setter, Supplier<Boolean> getter) {
        add(LumOption.builder(LumOptionType.BOOLEAN, key, label, def, getter, setter)
                .tooltip(tooltip)
                .build());
        return this;
    }

    public LumConfigContainer addIntSlider(String key, String label, int min, int max, int def,
                                            Consumer<Integer> setter, Supplier<Integer> getter) {
        return addIntSlider(key, label, "", min, max, 1, def, setter, getter);
    }

    public LumConfigContainer addIntSlider(String key, String label, String tooltip,
                                            int min, int max, int step, int def,
                                            Consumer<Integer> setter, Supplier<Integer> getter) {
        add(LumOption.builder(LumOptionType.INT_SLIDER, key, label, def, getter, setter)
                .tooltip(tooltip)
                .range(min, max, step)
                .build());
        return this;
    }

    public LumConfigContainer addDoubleSlider(String key, String label, double min, double max, double def,
                                               Consumer<Double> setter, Supplier<Double> getter) {
        return addDoubleSlider(key, label, "", min, max, 0.01, def, setter, getter);
    }

    public LumConfigContainer addDoubleSlider(String key, String label, String tooltip,
                                               double min, double max, double step, double def,
                                               Consumer<Double> setter, Supplier<Double> getter) {
        add(LumOption.builder(LumOptionType.DOUBLE_SLIDER, key, label, def, getter, setter)
                .tooltip(tooltip)
                .range(min, max, step)
                .build());
        return this;
    }

    public LumConfigContainer addText(String key, String label, String def,
                                       Consumer<String> setter, Supplier<String> getter) {
        return addText(key, label, "", 256, def, setter, getter);
    }

    public LumConfigContainer addText(String key, String label, String tooltip, int maxLength, String def,
                                       Consumer<String> setter, Supplier<String> getter) {
        add(LumOption.builder(LumOptionType.TEXT, key, label, def, getter, setter)
                .tooltip(tooltip)
                .maxLength(maxLength)
                .build());
        return this;
    }

    public <E extends Enum<E>> LumConfigContainer addEnum(String key, String label, Class<E> enumClass, E def,
                                                            Consumer<E> setter, Supplier<E> getter) {
        return addEnum(key, label, "", enumClass, def, setter, getter);
    }

    public <E extends Enum<E>> LumConfigContainer addEnum(String key, String label, String tooltip,
                                                            Class<E> enumClass, E def,
                                                            Consumer<E> setter, Supplier<E> getter) {
        add(LumOption.builder(LumOptionType.ENUM, key, label, def, getter, setter)
                .tooltip(tooltip)
                .enumValues(List.of(enumClass.getEnumConstants()))
                .build());
        return this;
    }

    /**
     * ARGB color option, e.g. {@code 0xFFAA00FF}. Stored and edited as a single
     * packed int so it round-trips cleanly through JSON.
     */
    public LumConfigContainer addColor(String key, String label, int defArgb,
                                        Consumer<Integer> setter, Supplier<Integer> getter) {
        return addColor(key, label, "", true, defArgb, setter, getter);
    }

    public LumConfigContainer addColor(String key, String label, String tooltip, boolean allowAlpha, int defArgb,
                                        Consumer<Integer> setter, Supplier<Integer> getter) {
        add(LumOption.builder(LumOptionType.COLOR, key, label, defArgb, getter, setter)
                .tooltip(tooltip)
                .allowAlpha(allowAlpha)
                .build());
        return this;
    }

    private void add(LumOption<?> option) {
        if (built) {
            throw new IllegalStateException("Cannot add options after build() was called for '" + modId + "'");
        }
        if (currentGroup != null) {
            currentGroup.add(option);
        } else {
            ungrouped.add(option);
        }
    }

    /**
     * @return this mod's option groups, in the order {@link #group(String)} was first
     * called for each. Used by LumLib's own screen renderer and storage layer.
     */
    public List<LumOptionGroup> groups() {
        return new ArrayList<>(groups.values());
    }

    /**
     * @return options added before any {@link #group(String)} call, i.e. options
     * that belong to no named group.
     */
    public List<LumOption<?>> ungrouped() {
        return ungrouped;
    }

    /**
     * @return every option registered on this container, ungrouped options first,
     * followed by each group's options in group order. Convenience for code that
     * needs to iterate all options regardless of grouping (e.g. serialization).
     */
    public List<LumOption<?>> allOptions() {
        List<LumOption<?>> result = new ArrayList<>(ungrouped);
        for (LumOptionGroup g : groups.values()) {
            result.addAll(g.options());
        }
        return result;
    }

    /**
     * Finalizes registration: loads any saved JSON for this mod and applies it to
     * your setters, then makes this mod visible in LumLib's mod list and (if
     * ModMenu is present) as its own ModMenu config entry.
     */
    public void build() {
        if (built) {
            throw new IllegalStateException("build() already called for '" + modId + "'");
        }
        built = true;
        LumConfigRegistry.register(this);
        LumConfigStorage.loadInto(this);
    }
}
