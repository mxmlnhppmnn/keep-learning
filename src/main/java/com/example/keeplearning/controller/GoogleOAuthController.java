package com.example.keeplearning.controller;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.service.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/google/oauth")
public class GoogleOAuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserRepository userRepository;

    private static final String SCOPE = "https://www.googleapis.com/auth/calendar.events";


    @GetMapping("/connect")
    public String connect(@RequestParam Long userId) {

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + SCOPE
                + "&access_type=offline"
                + "&prompt=consent"
                + "&state=" + userId;

        return "redirect:" + url;
    }



    @GetMapping("/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam String state
    ) {
        Long userId = Long.valueOf(state);

        try {
            // Austauschen gegen Access + Refresh Token
            String refreshToken = googleOAuthService.exchangeCodeForRefreshToken(code);

            if (refreshToken == null || refreshToken.isBlank()) {
                System.err.println("WARNUNG: Google hat keinen Refresh Token geliefert!");
                return "redirect:/user/" + userId + "?google=noRefreshToken";
            }

            // User laden
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden (id=" + userId + ")"));

            // Token speichern
            user.setGoogleRefreshToken(refreshToken);
            userRepository.save(user);

            System.out.println("Google Refresh Token gespeichert für User #" + userId);
            System.out.println("REFRESH TOKEN = " + refreshToken);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user/?google=error";
        }

        return "redirect:/user/?google=ok";
    }
}


