package com.fivefull.coordiback.service;

import com.fivefull.coordiback.util.ImageCache;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CrawlingService {

    private static final String BRANDI_URL = "https://brandi.co.kr/search?q=";
    private static final String LOOKPIN_URL = "https://www.lookpin.co.kr/search/results?keywords=";
    private final ImageCache cache = new ImageCache(60 * 60 * 1000); // Cache expiry: 1 hour

    public CrawlingService() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\kkr91\\Desktop\\coordi-on\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
    }

    public List<String> searchImageUrls(String keyword, String gender) {
        String searchUrl = gender.equalsIgnoreCase("남성") ? LOOKPIN_URL : BRANDI_URL;

        List<String> cachedUrls = cache.get(keyword);
        if (cachedUrls != null) {
            System.out.println("Cache hit for keyword: " + keyword);
            return cachedUrls;
        }
        System.out.println("Cache miss for keyword: " + keyword);

        List<String> imageUrls = new ArrayList<>();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get(searchUrl + keyword);
            Thread.sleep(2000);

            List<WebElement> imageElements;
            if (gender.equalsIgnoreCase("남성")) {
                imageElements = driver.findElements(By.cssSelector("#root img"));
                for (WebElement element : imageElements) {
                    String url = element.getAttribute("src");
                    if (url != null) {
                        imageUrls.add(url);
                    }

                    if (imageUrls.size() >= 5) {
                        break;
                    }
                }
            } else {
                imageElements = driver.findElements(By.cssSelector("div.thumb"));
                for (WebElement element : imageElements) {
                    String url = extractImageUrlFromStyle(element.getAttribute("style"));
                    if (url != null) {
                        imageUrls.add(url);
                    }

                    if (imageUrls.size() >= 5) {
                        break;
                    }
                }
            }

            cache.put(keyword, imageUrls);

        } catch (Exception e) {
            System.err.println("Error occurred during web scraping for keyword: " + keyword);
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return imageUrls;
    }

    private String extractImageUrlFromStyle(String style) {
        if (!style.contains(".webp")) {
            return null;
        }

        int start = style.indexOf("url(\"") + "url(\"".length();
        int end = style.indexOf("\")", start);

        if (start > -1 && end > start) {
            return style.substring(start, end);
        }

        System.err.println("Error extracting image URL from style attribute: " + style);
        return null;
    }
}
