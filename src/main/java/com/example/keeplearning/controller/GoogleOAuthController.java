package com.example.keeplearning.controller;

import com.example.keeplearning.entity.User;
import com.example.keeplearning.repository.UserRepository;
import com.example.keeplearning.service.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
/*
@Controller
@RequestMapping("/google/oauth")
public class GoogleOAuthController {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private static final String SCOPE =
            "https://www.googleapis.com/auth/calendar.events";

    @Autowired
    private GoogleOAuthService googleOAuthService;

    @Autowired
    private UserRepository userRepository;


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
            @RequestParam String state // userId
    ) {
        Long userId = Long.valueOf(state);

        try {
            // Token eintauschen
            String refreshToken = googleOAuthService.exchangeCodeForRefreshToken(code);

            // User finden
            User lehrer = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User nicht gefunden (id=" + userId + ")"));

            // Token speichern
            lehrer.setGoogleRefreshToken(refreshToken);
            userRepository.save(lehrer);

            System.out.println("Google Refresh Token gespeichert für Lehrer #" + userId);
            System.out.println("Token: " + refreshToken);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/user?google=error";
        }

        return "redirect:/user?google=ok";

    }
}*/

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

    // die endgültigen Scopes
    private static final String SCOPE = "https://www.googleapis.com/auth/calendar.events";


    @GetMapping("/connect")
    public String connect(@RequestParam Long userId) {

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri  // <–– nicht encodieren!
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
            @RequestParam String state // userId
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


