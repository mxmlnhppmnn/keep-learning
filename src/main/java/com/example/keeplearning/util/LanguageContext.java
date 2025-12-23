package com.example.keeplearning.util;

public class LanguageContext {

    private static final ThreadLocal<String> CURRENT_LANGUAGE =
            ThreadLocal.withInitial(() -> "DE");

    public static void setLanguage(String language) {
        CURRENT_LANGUAGE.set(language);
    }

    public static String getLanguage() {
        return CURRENT_LANGUAGE.get();
    }

    public static void clear() {
        CURRENT_LANGUAGE.remove();
    }
}
