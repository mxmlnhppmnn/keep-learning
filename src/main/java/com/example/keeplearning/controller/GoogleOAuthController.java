package com.example.keeplearning.controller;

import com.example.keeplearning.repository.BenutzerRepository;
import com.example.keeplearning.service.GoogleOAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.keeplearning.entity.Benutzer;

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
    private BenutzerRepository benutzerRepository;


    @GetMapping("/connect")
    public String connect(@RequestParam Long userId) {

        String url = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=" + SCOPE
                + "&access_type=offline"     // wichtig für refresh token
                + "&prompt=consent"          // erzwingt refresh token beim ersten Mal
                + "&state=" + userId;        // merken, welcher Lehrer sich verbindet

        return "redirect:" + url;
    }
    @GetMapping("/callback")
    public String callback(
            @RequestParam String code,
            @RequestParam String state   // userId
    ) {
        Long userId = Long.valueOf(state);

        try {
            // Token eintauschen
            String refreshToken = googleOAuthService.exchangeCodeForRefreshToken(code);

            // User holen
            Benutzer lehrer = benutzerRepository.findById(userId)
                    .orElseThrow();

            // Refresh Token speichern
            lehrer.setGoogleRefreshToken(refreshToken);
            benutzerRepository.save(lehrer);

            System.out.println("Google Refresh Token gespeichert für Lehrer #" + userId);
            System.out.println("Token: " + refreshToken);

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/lehrer/" + userId + "/einstellungen?google=error";
        }

        return "redirect:/lehrer/" + userId + "/einstellungen?google=ok";
    }


}
