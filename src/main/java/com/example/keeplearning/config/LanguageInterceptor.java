package com.example.keeplearning.config;

import com.example.keeplearning.util.LanguageContext;
import com.example.keeplearning.util.LanguageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

//sorgt dafür, dass jeder Request eine Sprache hat
@Component
public class LanguageInterceptor implements HandlerInterceptor {

    private final LanguageResolver languageResolver;

    public LanguageInterceptor(LanguageResolver languageResolver) {
        this.languageResolver = languageResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {

        // Sprache ermitteln und im Context setzen
        languageResolver.resolve(request);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {

        //ThreadLocal aufräumen
        //da sonst unerwartet die falsche Sprache aus einem vorherigen Request angezeigt werden könnte
        LanguageContext.clear();
    }
}
