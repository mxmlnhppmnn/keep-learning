package com.example.keeplearning.security;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Component
//für lesbare Fehlermeldungen auf dem Loginscreen bei sperren/löschen
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private static final DateTimeFormatter LOCK_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final UserRepository userRepository;

    public CustomAuthenticationFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, //der login versuch
            HttpServletResponse response,
            AuthenticationException exception) //exception kommt direkt aus spring security
            throws IOException, ServletException {

        String errorMessage = null;

        if (exception instanceof LockedException) {
            User user = userRepository.findByEmail(
                    request.getParameter("username")
            ).orElse(null);

            if (user != null) {

                StringBuilder message = new StringBuilder("Dein Account ist ");

                //Unterschiedliche Nachricht jenachdem wie lange der Account gesperrt ist
                if (user.getLockedUntil() == null) {
                    message.append("unbefristet gesperrt");
                } else {
                    message.append("gesperrt bis ")
                            .append(user.getLockedUntil()
                                    .format(LOCK_DATE_FORMAT));
                }

                // opt. Sperrgrund
                if (user.getLockReason() != null) {
                    message.append(": ")
                            .append(user.getLockReason());
                }

                errorMessage = message.toString();

            } else {
                //zur Sicherheit
                //falls z.b. der user während dem login vorgang gelöscht wird
                errorMessage = "Dein Account ist derzeit gesperrt.";
            }
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
