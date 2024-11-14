package com.fivefull.coordiback.service;

import com.fivefull.coordiback.entity.ImageUrl;
import com.fivefull.coordiback.repository.ImageUrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CrawlingService {

    private static final String FEMALE_URL = "https://shop.29cm.co.kr/search?keyword=";
    private static final String MALE_URL = "https://www.lookpin.co.kr/search/results?keywords=";

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

    @Autowired
    private ImageUrlRepository imageUrlRepository;

    public CrawlingService() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\kkr91\\Desktop\\coordi-on\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
    }

    public void initializeImageUrls() {

        imageUrlRepository.deleteAll();
        System.out.println("image 초기화");

        for (Map.Entry<String, List<String>> entry : maleOptions.entrySet()) {
            saveCategoryImages(entry.getKey(), entry.getValue(), "남성");
        }
        for (Map.Entry<String, List<String>> entry : femaleOptions.entrySet()) {
            saveCategoryImages(entry.getKey(), entry.getValue(), "여성");
        }
        System.out.println("Image URL initialization completed.");
    }


    private void saveCategoryImages(String category, List<String> items, String gender) {
        for (String item : items) {
            if (imageUrlRepository.findByCategoryAndItemAndGender(category, item, gender).isEmpty()) {
                List<String> urls = crawlImagesForItem(item, gender);
                ImageUrl imageUrl = new ImageUrl();
                imageUrl.setCategory(category);
                imageUrl.setItem(item);
                imageUrl.setGender(gender);
                imageUrl.setUrls(urls);
                imageUrlRepository.save(imageUrl);
            }
        }
    }

    private List<String> crawlImagesForItem(String item, String gender) {
        String searchUrl = gender.equalsIgnoreCase("남성") ? MALE_URL : FEMALE_URL;
        List<String> imageUrls = new ArrayList<>();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(searchUrl + item);
            Thread.sleep(2000);

            String selector = gender.equalsIgnoreCase("남성") ? "#root img" : "#__next img";
            List<WebElement> imageElements = driver.findElements(By.cssSelector(selector));

            for (WebElement element : imageElements) {
                String url = element.getAttribute("src");
                if (url != null) {
                    imageUrls.add(url);
                }
                if (imageUrls.size() >= 15) break;
            }

            System.out.println("Crawled URLs for item " + item + ": " + imageUrls);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return imageUrls;
    }


    public List<String> getImageUrls(String category, String item, String gender, int startIndex, int endIndex) {
        System.out.println("Fetching URLs with parameters: category=" + category + ", item=" + item + ", gender=" + gender);

        Optional<ImageUrl> imageUrlOpt = imageUrlRepository.findByCategoryAndItemAndGender(category, item, gender);
        if (imageUrlOpt.isPresent()) {
            List<String> urls = imageUrlOpt.get().getUrls();
            return urls.subList(Math.min(startIndex, urls.size()), Math.min(endIndex, urls.size()));
        }

        System.out.println("No URLs found for category=" + category + ", item=" + item + ", gender=" + gender);
        return Collections.emptyList();
    }
}
