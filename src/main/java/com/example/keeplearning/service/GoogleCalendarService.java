package com.example.keeplearning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class GoogleCalendarService {

    private final GoogleOAuthService oauthService;
    private final ObjectMapper mapper = new ObjectMapper();

    public GoogleCalendarService(GoogleOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    public void createCalendarEvent(String refreshToken,
                                    LocalDate date,
                                    LocalTime start,
                                    LocalTime end,
                                    String summary,
                                    String description) throws Exception {

        //  Access Token erzeugen
        String accessToken = oauthService.refreshAccessToken(refreshToken);

        // google API URL
        URL url = new URL("https://www.googleapis.com/calendar/v3/calendars/primary/events");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");

        // event JSON bauen
        String startDt = date + "T" + start + ":00+01:00";
        String endDt   = date + "T" + end + ":00+01:00";

        String eventJson = """
        {
          "summary": "%s",
          "description": "%s",
          "start": { "dateTime": "%s" },
          "end": { "dateTime": "%s" }
        }
        """.formatted(summary, description, startDt, endDt);

        // senden
        try (OutputStream os = conn.getOutputStream()) {
            os.write(eventJson.getBytes(StandardCharsets.UTF_8));
        }

        JsonNode json = mapper.readTree(conn.getInputStream());
        System.out.println("GOOGLE EVENT CREATED: " + json);
    }
}
