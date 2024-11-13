//package com.fivefull.coordiback.util;
//
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//
//import java.io.FileWriter;
//import java.util.concurrent.TimeUnit;
//
//public class crawltest {
//
//    private static final String LOOKPIN_URL = "https://m.a-bly.com/search?screen_name=SEARCH_RESULT&keyword=니트";
//
//    public static void main(String[] args) {
//        validateHtmlWithSelenium();
//    }
//
//    public static void validateHtmlWithSelenium() {
//        System.setProperty("webdriver.chrome.driver", "C:\\Users\\kkr91\\Desktop\\coordi-on\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe");
//
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//
//        WebDriver driver = new ChromeDriver(options);
//        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//
//        try {
//            driver.get(LOOKPIN_URL);
//
//            WebElement rootElement = driver.findElement(By.id("root"));
//
//            String pageSource = driver.getPageSource();
//
//            try (FileWriter writer = new FileWriter("selenium_test.html")) {
//                writer.write(pageSource);
//            }
//
//            System.out.println(" 완료");
//
//        } catch (Exception e) {
//            System.out.println("오류 발생: " + e.getMessage());
//        } finally {
//            driver.quit();
//        }
//    }
//}
