package dev.lumyrix.lumlib.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named section of related options, rendered as a header followed by its
 * widgets in the generated config screen. Groups are created implicitly by
 * {@link LumConfigContainer#group(String)} — there is no public constructor.
 */
public final class LumOptionGroup {

    private final String title;
    private final List<LumOption<?>> options = new ArrayList<>();

    LumOptionGroup(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }

    public List<LumOption<?>> options() {
        return Collections.unmodifiableList(options);
    }

    void add(LumOption<?> option) {
        options.add(option);
    }
}
