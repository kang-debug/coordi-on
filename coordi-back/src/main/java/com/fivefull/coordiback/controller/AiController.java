package com.fivefull.coordiback.controller;

import com.fivefull.coordiback.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/api/ai-fashion")
    public ResponseEntity<Map<String, Object>> getOutfitRecommendation(@RequestBody Map<String, Object> requestParams) {
        int age = (int) requestParams.get("age");
        String gender = (String) requestParams.get("gender");
        String temperature = (String) requestParams.get("temperature");
        String weatherCondition = (String) requestParams.get("weatherCondition");
        String maxTemp = (String) requestParams.get("maxTemp");
        String minTemp = (String) requestParams.get("minTemp");
        String rain = (String) requestParams.get("rain");

        Map<String, Object> result = new HashMap<>();

        String weatherComment = aiService.getWeatherComment(temperature, weatherCondition, maxTemp, minTemp, rain);
        result.put("weatherComment", weatherComment != null ? weatherComment : "날씨 코멘트를 가져올 수 없습니다.");

        Map<String, String> outfitRecommendation = aiService.getOutfitRecommendation(age, gender, temperature, weatherCondition, maxTemp, minTemp);
        result.put("outfitRecommendation", outfitRecommendation);

        return ResponseEntity.ok(result);
    }
}
