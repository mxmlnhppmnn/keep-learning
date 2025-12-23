package com.example.keeplearning.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class LanguageResolver {

    public String resolve(HttpServletRequest request) {
        String lang = request.getParameter("lang");

        // Deutsch als Default
        if (lang == null || lang.isBlank()) {
            LanguageContext.setLanguage("DE");
            return "DE";
        }

        String resolvedLang = lang.toUpperCase(); // DeepL erwartet EN / DE
        LanguageContext.setLanguage(resolvedLang);
        return resolvedLang;
    }
}
