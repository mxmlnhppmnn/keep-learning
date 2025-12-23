package com.example.keeplearning.service.translation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

//kommuniziert  mit der API
@Service
public class DeepLTranslationService {

    private final String apiKey;
    private final RestTemplate restTemplate = new RestTemplate(); //http um deepl aufzurufen

    //api key aus application.properties lesen
    public DeepLTranslationService(
            @Value("${deepl.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String translate(String text, String targetLang) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String url = "https://api-free.deepl.com/v2/translate";

        // form-Daten statt JSON
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("auth_key", apiKey);
        form.add("text", text);
        form.add("source_lang", "DE");
        form.add("target_lang", targetLang);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        //Antwort von DeepL

        Map response = restTemplate.postForObject(url, request, Map.class);

        List<Map<String, String>> translations = (List<Map<String, String>>) response.get("translations");

        return translations.get(0).get("text"); //wir senden immer nur einen wert per request
    }
}
