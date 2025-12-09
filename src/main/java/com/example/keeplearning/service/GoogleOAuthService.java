package com.example.keeplearning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleOAuthService {

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final ObjectMapper mapper = new ObjectMapper();

    public String exchangeCodeForRefreshToken(String code) throws Exception {

        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "code=" + code
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&grant_type=authorization_code";

        // Body senden
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        // Antwort lesen
        JsonNode json = mapper.readTree(conn.getInputStream());

        // refresh_token extrahieren
        return json.has("refresh_token")
                ? json.get("refresh_token").asText()
                : null;
    }
    public String refreshAccessToken(String refreshToken) throws Exception {

        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + refreshToken
                + "&grant_type=refresh_token";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        JsonNode json = mapper.readTree(conn.getInputStream());

        return json.get("access_token").asText();
    }

}
