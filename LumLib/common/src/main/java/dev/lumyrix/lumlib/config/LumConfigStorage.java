package dev.lumyrix.lumlib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.lumyrix.lumlib.api.LumConfigContainer;
import dev.lumyrix.lumlib.api.LumOption;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Reads and writes each registered mod's options to
 * {@code config/lumlib/<modId>.json} automatically. Mods never touch Gson,
 * files, or serialization directly — every {@code addXxx} setter/getter pair
 * given to {@link LumConfigContainer} is all that's needed.
 */
public final class LumConfigStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger("lumlib/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("lumlib");

    private LumConfigStorage() {
    }

    private static Path pathFor(String modId) {
        return CONFIG_DIR.resolve(modId + ".json");
    }

    /**
     * Loads {@code config/lumlib/<modId>.json} if it exists and applies each saved
     * value to the option's setter. Missing files, missing keys, and type mismatches
     * are all tolerated — defaults simply stay in place and a warning is logged.
     */
    public static void loadInto(LumConfigContainer container) {
        Path path = pathFor(container.modId());
        if (!Files.isRegularFile(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || !root.isJsonObject()) {
                return;
            }
            JsonObject obj = root.getAsJsonObject();
            for (LumOption<?> option : container.allOptions()) {
                if (!obj.has(option.key())) {
                    continue;
                }
                applyValue(option, obj.get(option.key()));
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("[LumLib] Failed to load config for '{}': {}", container.modId(), e.toString());
        }
    }

    private static void applyValue(LumOption<?> option, JsonElement element) {
        try {
            switch (option.type()) {
                case BOOLEAN -> option.setRaw(element.getAsBoolean());
                case INT_SLIDER, COLOR -> option.setRaw(element.getAsInt());
                case DOUBLE_SLIDER -> option.setRaw(element.getAsDouble());
                case TEXT -> option.setRaw(element.getAsString());
                case ENUM -> {
                    String name = element.getAsString();
                    for (Enum<?> constant : option.enumValues()) {
                        if (constant.name().equals(name)) {
                            option.setRaw(constant);
                            return;
                        }
                    }
                    LOGGER.warn("[LumLib] Unknown enum value '{}' for option '{}', keeping default", name, option.key());
                }
            }
        } catch (RuntimeException e) {
            LOGGER.warn("[LumLib] Bad saved value for option '{}': {}", option.key(), e.toString());
        }
    }

    /**
     * Writes every current value for the given mod's registered options to
     * {@code config/lumlib/<modId>.json}. Called automatically when a LumLib
     * config screen closes; mods may also call it manually after changing
     * values programmatically.
     */
    public static void save(LumConfigContainer container) {
        JsonObject obj = new JsonObject();
        for (LumOption<?> option : container.allOptions()) {
            writeValue(obj, option);
        }
        try {
            Files.createDirectories(CONFIG_DIR);
            try (Writer writer = Files.newBufferedWriter(pathFor(container.modId()), StandardCharsets.UTF_8)) {
                GSON.toJson(obj, writer);
            }
        } catch (IOException e) {
            LOGGER.error("[LumLib] Failed to save config for '{}': {}", container.modId(), e.toString());
        }
    }

    private static void writeValue(JsonObject obj, LumOption<?> option) {
        switch (option.type()) {
            case BOOLEAN -> obj.addProperty(option.key(), (Boolean) option.get());
            case INT_SLIDER, COLOR -> obj.addProperty(option.key(), (Integer) option.get());
            case DOUBLE_SLIDER -> obj.addProperty(option.key(), (Double) option.get());
            case TEXT -> obj.addProperty(option.key(), (String) option.get());
            case ENUM -> obj.addProperty(option.key(), ((Enum<?>) option.get()).name());
        }
    }
}
