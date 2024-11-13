package com.fivefull.coordiback.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @Autowired
    private CrawlingService crawlingService;

    // 캐시: 추천 아이템당 이미지 URL 저장 (키워드별로 최대 8개)
    private final Map<String, List<String>> imageCache = new HashMap<>();

    private static final Map<String, List<String>> maleOptions = Map.of(
            "상의", List.of("니트", "티셔츠", "후드"),
            "하의", List.of("청바지", "면바지", "슬랙스"),
            "아우터", List.of("무스탕", "항공 점퍼", "숏패딩", "롱패딩"),
            "신발", List.of("운동화", "부츠", "로퍼")
    );

    private static final Map<String, List<String>> femaleOptions = Map.of(
            "상의", List.of("블라우스", "티셔츠", "맨투맨"),
            "하의", List.of("스커트", "데님팬츠", "코튼팬츠"),
            "아우터", List.of("코트", "가디건", "트렌치코트"),
            "신발", List.of("스니커즈", "힐", "부츠", "운동화")
    );

    public String getWeatherComment(String temperature, String weatherCondition, String maxTemp, String minTemp, String rain) {
        String prompt = String.format(
                "오늘의 날씨에 대한 코멘트를 알려줘. 강수확률과 하늘 상태, 최고, 최저, 현재 기온에 대한 코멘트를 포함해. 답변 형식은 느낌표(!)로 감싸서 '!(코멘트)!' 형식으로 답변해 주고 코멘트는 50이상 70자 이하로 하고 끝에 이모티콘을 달아줘. 현재 기온은 %s°C, 날씨 상태는 %s, 최고기온은 %s°C, 최저기온은 %s°C, 강수확률은 %s%%입니다.",
                temperature, weatherCondition, maxTemp, minTemp, rain
        );
        return sendOpenAiRequest(prompt, "!");
    }

    public Map<String, Map<String, Object>> getOutfitRecommendation(int age, String gender, String temperature, String weatherCondition, String maxTemp, String minTemp) {
        Map<String, List<String>> options = gender.equalsIgnoreCase("남성") ? maleOptions : femaleOptions;

        String prompt = String.format(
                "나이는 %d세, 성별은 %s, 현재 기온은 %s°C, 날씨 상태는 %s, 최고기온은 %s°C, 최저기온은 %s°C. " +
                        "아래 항목에서 상의, 하의, 아우터, 신발을 선택하여 JSON 형식으로 추천해" +
                        "예: { \"상의\": \"아이템\", \"하의\": \"아이템\", \"아우터\": \"아이템\", \"신발\": \"아이템\" }\n\n" +
                        "상의 옵션: %s\n" +
                        "하의 옵션: %s\n" +
                        "아우터 옵션: %s\n" +
                        "신발 옵션: %s",
                age, gender, temperature, weatherCondition, maxTemp, minTemp,
                String.join(", ", options.get("상의")),
                String.join(", ", options.get("하의")),
                String.join(", ", options.get("아우터")),
                String.join(", ", options.get("신발"))
        );

        JsonNode recommendation = sendOpenAiRequestForJson(prompt);

        Map<String, Map<String, Object>> result = new HashMap<>();
        if (recommendation != null) {
            recommendation.fields().forEachRemaining(entry -> {
                String item = entry.getValue().asText();

                List<String> imageUrls = imageCache.computeIfAbsent(item, k -> crawlingService.searchImageUrls(k, gender));

                Map<String, Object> itemData = new HashMap<>();
                itemData.put("item", item);
                itemData.put("imageUrls", imageUrls); // 이미지 URL 리스트 추가

                result.put(entry.getKey(), itemData);
            });
        }

        return result;
    }

    private String sendOpenAiRequest(String prompt, String delimiter) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(openAiApiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = new ObjectMapper().readTree(response.getBody());
                String content = rootNode.path("choices").get(0).path("message").path("content").asText();

                int start = content.indexOf(delimiter);
                int end = content.lastIndexOf(delimiter);
                if (start != -1 && end != -1 && start != end) {
                    return content.substring(start + 1, end).trim();
                }
                return content.trim();
            } else {
                System.err.println("OpenAI API responded with an error: " + response.getBody());
                return null;
            }
        } catch (RestClientException | JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JsonNode sendOpenAiRequestForJson(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(openAiApiUrl, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = new ObjectMapper().readTree(response.getBody());
                String jsonResponse = rootNode.path("choices").get(0).path("message").path("content").asText();

                return new ObjectMapper().readTree(jsonResponse.replaceAll("```json", "").replaceAll("```", "").trim());
            } else {
                System.err.println("OpenAI API responded with an error: " + response.getBody());
                return null;
            }
        } catch (RestClientException | JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
