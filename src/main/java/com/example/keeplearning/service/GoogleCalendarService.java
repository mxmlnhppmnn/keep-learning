package com.example.keeplearning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;


import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoogleCalendarService {

    private final GoogleOAuthService oauthService;
    private final ObjectMapper mapper = new ObjectMapper();

    public GoogleCalendarService(GoogleOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    public void createCalendarEvent(
            String refreshToken,
            LocalDate date,
            LocalTime start,
            LocalTime end,
            String summary,
            String description
    ) throws Exception {

        // Access Token aus Refresh Token erzeugen
        String accessToken = oauthService.refreshAccessToken(refreshToken);

        // Verbindungsaufbau
        URL url = new URL("https://www.googleapis.com/calendar/v3/calendars/primary/events");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        // Korrektes Datumsformat (wichtig! google calendar akzeptiert es nur so!)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        ZoneId zone = ZoneId.of("Europe/Berlin");

        String startDt = ZonedDateTime.of(date, start, zone).format(formatter);
        String endDt   = ZonedDateTime.of(date, end,   zone).format(formatter);

        // JSON Payload erzeugen
        String eventJson = """
        {
          "summary": "%s",
          "description": "%s",
          "start": {
            "dateTime": "%s",
            "timeZone": "Europe/Berlin"
          },
          "end": {
            "dateTime": "%s",
            "timeZone": "Europe/Berlin"
          }
        }
        """.formatted(summary, description, startDt, endDt);


        // Body u. Content Length vorbereiten und senden
        byte[] bodyBytes = eventJson.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
            os.flush();
        }

        // Antwort von Google
        int status = conn.getResponseCode();

        InputStream is = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        String responseRaw = new String(is.readAllBytes(), StandardCharsets.UTF_8);


        if (status >= 400) {
            throw new RuntimeException("Google Calendar error: " + responseRaw);
        }

        // Zur Sicherheit erstelltes Event ausgeben
        JsonNode json = mapper.readTree(responseRaw);
        System.out.println("GOOGLE EVENT CREATED SUCCESSFULLY: " + json);
    }

}
