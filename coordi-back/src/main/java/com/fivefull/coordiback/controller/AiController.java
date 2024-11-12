package com.fivefull.coordiback.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AiController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @GetMapping("/api/ai-fashion")
    public ResponseEntity<String> getOutfitRecommendation(
            @RequestParam("age") int age,
            @RequestParam("gender") String gender,
            @RequestParam("temperature") String temperature,
            @RequestParam("weatherCondition") String weatherCondition,
            @RequestParam("maxTemp") String maxTemp,
            @RequestParam("minTemp") String minTemp
    ) {
        String prompt = String.format(
                "오늘 날씨에 맞는 옷을 추천해줘. 나이는 %d세, 성별은 %s이고, 현재 기온은 %s°C, 날씨 상태는 %s, 최고기온은 %s°C, 최저기온은 %s°C.",
                age, gender, temperature, weatherCondition, maxTemp, minTemp
        );

        System.out.println(prompt);
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a helpful coordinator."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(openAiApiUrl, entity, String.class);

        return ResponseEntity.ok(response.getBody());
    }
}
