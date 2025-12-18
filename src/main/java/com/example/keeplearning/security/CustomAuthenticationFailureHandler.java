package com.example.keeplearning.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
//für lesbare Fehlermeldungen auf dem Loginscreen bei sperren/löschen
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, //der login versuch
            HttpServletResponse response,
            AuthenticationException exception) //exception kommt direkt aus spring security
            throws IOException, ServletException {

        String errorMessage = null;

        if (exception instanceof LockedException) {
            errorMessage = "Dein Account ist derzeit gesperrt.";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Dein Account wurde deaktiviert."; //z.b. bei status = deleted
        }


        //muss gelöscht werden, da die errorMessages sonst die komplette Session bleiben
        if (errorMessage != null) {
            // fehler -> setzen
            request.getSession().setAttribute("LOGIN_ERROR", errorMessage);
        } else {
            // Kein Status-Fehler - > dann wieder löschen
            request.getSession().removeAttribute("LOGIN_ERROR");
        }
        response.sendRedirect("/login?error");
    }

}
