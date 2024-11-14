package com.fivefull.coordiback;

import com.fivefull.coordiback.service.CrawlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CrawlingData implements CommandLineRunner {

    @Autowired
    private CrawlingService crawlingService;

    @Override
    public void run(String... args) throws Exception {
        crawlingService.initializeImageUrls();
    }
}