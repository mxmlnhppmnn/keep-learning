package com.example.keeplearning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleOAuthService {

    /*@Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;*/

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final ObjectMapper mapper = new ObjectMapper();

    public String exchangeCodeForRefreshToken(String code) throws Exception {

        //Verbindungsaufbau
        URL url = new URL("https://oauth2.googleapis.com/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        //Body vorbereiten
        String body = "code=" + code
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&redirect_uri=" + redirectUri
                + "&grant_type=authorization_code";

        // Body senden
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(StandardCharsets.UTF_8));
        }

        //Antwort von Google lesen
        int status = conn.getResponseCode();

        //Falls Fehler aufgetreten sind, nimmt er den Fehlerdatenstrom, sonst den normalen
        InputStream is = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        //Bytes aus dem Stream lesen
        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        //macht ein json objekt draus
        JsonNode json = mapper.readTree(raw);


        // refresh_token extrahieren
        return json.has("refresh_token")
                ? json.get("refresh_token").asText()
                : null;
    }
    public String refreshAccessToken(String refreshToken) throws Exception {

        //Verbindungsaufbau
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

        //Antwort von Google
        int status = conn.getResponseCode();

        InputStream is = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        String raw = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        JsonNode json = mapper.readTree(raw);

        return json.get("access_token").asText();
    }

}
