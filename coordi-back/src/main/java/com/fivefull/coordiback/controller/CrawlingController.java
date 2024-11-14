package com.fivefull.coordiback.controller;

import com.fivefull.coordiback.service.CrawlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CrawlingController {

    @Autowired
    private CrawlingService crawlingService;

    @PostMapping("/crawling")
    public ResponseEntity<Map<String, Object>> getCategoryImages(@RequestBody Map<String, Object> params) {
        String category = (String) params.get("category");
        String item = (String) params.get("item");
        String gender = (String) params.get("gender");

        if (item == null || item.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("category", category);
            response.put("item", item);
            response.put("imageUrls", Collections.emptyList());
            return ResponseEntity.ok(response);
        }

        List<String> imageUrls = crawlingService.getImageUrls(category, item, gender, 0, 8);
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("item", item);
        response.put("imageUrls", imageUrls);

        return ResponseEntity.ok(response);
    }

}
