package dev.lumyrix.lumlib.config;

import net.minecraft.locale.Language;

/** Resolve textos traduzidos dos lang files do LumLib; se a chave nao existir, usa o texto padrao. */
public final class LumI18n {

    private LumI18n() {
    }

    public static String tr(String key, String fallback) {
        try {
            Language language = Language.getInstance();
            if (language.has(key)) {
                return language.getOrDefault(key);
            }
        } catch (Throwable ignored) {
        }
        return fallback;
    }
}
