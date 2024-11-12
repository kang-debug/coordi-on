package com.fivefull.coordiback.controller;

import com.fivefull.coordiback.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @PostMapping("/api/weather")
    public ResponseEntity<Map<String, Object>> getWeatherData(@RequestBody Map<String, Double> location) {
        try {
            Double latitude = location.get("latitude");
            Double longitude = location.get("longitude");

            Map<String, String> weatherData = weatherService.fetchWeatherData(latitude, longitude);
            return ResponseEntity.ok(Map.of("values", weatherData));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "An error occurred while retrieving weather data."));
        }
    }
}
