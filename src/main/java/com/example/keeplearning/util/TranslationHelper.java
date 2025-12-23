package com.example.keeplearning.util;

import com.example.keeplearning.service.translation.DeepLTranslationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//ist zwischen View und Service das Bindeglied
@Component("t")
public class TranslationHelper {

    private final DeepLTranslationService translationService;

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TranslationHelper(DeepLTranslationService translationService) {
        this.translationService = translationService;
    }

    public String t(String text) {
        String lang = LanguageContext.getLanguage(); // DE / EN / FR
        String key = lang + "|" + text;

        // ✅ Cache-Hit
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        // ❌ Kein Cache → DeepL aufrufen
        String translated = translationService.translate(text, lang);

        cache.put(key, translated);
        return translated;
    }
}
