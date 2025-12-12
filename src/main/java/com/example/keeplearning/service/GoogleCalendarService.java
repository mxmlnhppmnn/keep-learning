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

        // 1) Access Token aus Refresh Token erzeugen
        String accessToken = oauthService.refreshAccessToken(refreshToken);

        // 2) Connection vorbereiten
        URL url = new URL("https://www.googleapis.com/calendar/v3/calendars/primary/events");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");

        // 3) Datumsformat korrekt erzeugen (mit Sekunden!)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        ZoneId zone = ZoneId.of("Europe/Berlin");

        String startDt = ZonedDateTime.of(date, start, zone).format(formatter);
        String endDt   = ZonedDateTime.of(date, end,   zone).format(formatter);

        // 4) JSON Payload erzeugen
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

        // DEBUG: Das JSON ausgeben
        System.out.println("EVENT JSON SENT TO GOOGLE:");
        System.out.println(eventJson);

        // 5) Body vorbereiten + Content-Length setzen
        byte[] bodyBytes = eventJson.getBytes(StandardCharsets.UTF_8);
        conn.setRequestProperty("Content-Length", String.valueOf(bodyBytes.length));

        // 6) Body senden
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bodyBytes);
            os.flush();
        }

        // 7) Antwort von Google lesen
        int status = conn.getResponseCode();

        InputStream is = (status >= 400)
                ? conn.getErrorStream()
                : conn.getInputStream();

        String responseRaw = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        System.out.println("GOOGLE EVENT RESPONSE:");
        System.out.println(responseRaw);

        if (status >= 400) {
            throw new RuntimeException("Google Calendar error: " + responseRaw);
        }

        // 8) Erfolgreich!
        JsonNode json = mapper.readTree(responseRaw);
        System.out.println("GOOGLE EVENT CREATED SUCCESSFULLY: " + json);
    }

}
