package com.example.keeplearning.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
//damit lang auch in kombination mit z.B. query möglich ist
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return (uri == null) ? "" : uri;
    }

    @ModelAttribute("existingQuery")
    public String existingQuery(HttpServletRequest request) {
        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return "";
        }
        // vorhandenes lang entfernen
        return query.replaceAll("(&?lang=[^&]*)", "");
    }

    @ModelAttribute("queryPrefix")
    public String queryPrefix(@ModelAttribute("existingQuery") String existingQuery) {
        return existingQuery.isEmpty() ? "?" : "?" + existingQuery + "&";
    }
}
