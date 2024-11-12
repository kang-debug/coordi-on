package com.fivefull.coordiback.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivefull.coordiback.entity.WeatherData;
import com.fivefull.coordiback.repository.WeatherDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Transactional
    public Map<String, String> fetchWeatherData(Double latitude, Double longitude) throws Exception {
        Map<String, Integer> grid = convertToGrid(latitude, longitude);
        int nx = grid.get("nx");
        int ny = grid.get("ny");
        LocalDate currentDate = LocalDate.now();

        System.out.println("Fetching weather data for date: " + currentDate + ", nx: " + nx + ", ny: " + ny);

        // 같은 date, nx, ny의 데이터 중 id가 가장 낮은 항목을 조회 ( 가끔 2번 씩 실행되면 오류날 거를 보정 )
        Optional<WeatherData> cachedData = weatherDataRepository.findFirstByDateAndNxAndNyOrderByIdAsc(currentDate, nx, ny);
        Map<String, String> weatherData = new HashMap<>();

        if (cachedData.isPresent()) {
            weatherData.put("TMX", cachedData.get().getTmx());
            weatherData.put("TMN", cachedData.get().getTmn());
        } else {
            Map<String, String> tmxTmnData = fetchTmxTmnFromApi(nx, ny, currentDate);

            if (tmxTmnData.isEmpty()) {
                throw new RuntimeException("Failed to retrieve TMX/TMN data from API.");
            }

            // 새로운 데이터를 저장하기 전에 동일한 데이터가 저장되지 않았는지 확인
            if (!weatherDataRepository.findFirstByDateAndNxAndNyOrderByIdAsc(currentDate, nx, ny).isPresent()) {
                WeatherData dataToSave = new WeatherData();
                dataToSave.setDate(currentDate);
                dataToSave.setNx(nx);
                dataToSave.setNy(ny);
                dataToSave.setTmx(tmxTmnData.get("TMX"));
                dataToSave.setTmn(tmxTmnData.get("TMN"));
                weatherDataRepository.save(dataToSave);

                System.out.println("New data saved to cache: " + dataToSave);
            }
            weatherData.putAll(tmxTmnData);
        }

        Map<String, String> realTimeData = fetchRealTimeWeatherData(nx, ny);
        if (realTimeData.isEmpty()) {
            System.err.println("Failed to retrieve real-time weather data from API.");
            throw new RuntimeException("Failed to retrieve real-time weather data from API.");
        }
        weatherData.putAll(realTimeData);

        return weatherData;
    }

    private Map<String, String> fetchTmxTmnFromApi(int nx, int ny, LocalDate date) throws Exception {
        String baseDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String url = apiUrl + "?serviceKey=" + URLEncoder.encode(apiKey, "UTF-8") +
                "&pageNo=1&numOfRows=500&dataType=JSON&base_date=" + baseDate +
                "&base_time=0200&nx=" + nx + "&ny=" + ny;

        System.out.println("Fetching TMX/TMN from API: " + url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        System.out.println("TMX/TMN API response code: " + responseCode);

        BufferedReader rd = responseCode >= 200 && responseCode <= 300
                ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            responseBuilder.append(line);
        }
        rd.close();
        conn.disconnect();


        Map<String, String> tmxTmnData = parseTmxTmn(responseBuilder.toString());
        if (tmxTmnData.isEmpty()) {
            System.err.println("Failed to parse TMX/TMN data from API response.");
        }
        return tmxTmnData;
    }

    private Map<String, String> fetchRealTimeWeatherData(int nx, int ny) throws Exception {
        String baseDate = getBaseDate();
        String baseTime = getClosestBaseTime();
        String url = apiUrl + "?serviceKey=" + URLEncoder.encode(apiKey, "UTF-8") +
                "&pageNo=1&numOfRows=10&dataType=JSON&base_date=" + baseDate +
                "&base_time=" + baseTime + "&nx=" + nx + "&ny=" + ny;

        System.out.println("Fetching real-time weather from API: " + url);

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        System.out.println("Real-time weather API response code: " + responseCode);

        BufferedReader rd = responseCode >= 200 && responseCode <= 300
                ? new BufferedReader(new InputStreamReader(conn.getInputStream()))
                : new BufferedReader(new InputStreamReader(conn.getErrorStream()));

        StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            responseBuilder.append(line);
        }
        rd.close();
        conn.disconnect();


        Map<String, String> realTimeData = parseRealTimeWeather(responseBuilder.toString());
        if (realTimeData.isEmpty()) {
            System.err.println("Failed to parse real-time weather data from API response.");
        }
        return realTimeData;
    }

    private Map<String, Integer> convertToGrid(double lat, double lon) {
        Map<String, Integer> grid = new HashMap<>();
        double RE = 6371.00877;
        double GRID = 5.0;
        double SLAT1 = 30.0;
        double SLAT2 = 60.0;
        double OLON = 126.0;
        double OLAT = 38.0;
        double XO = 43;
        double YO = 136;

        double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);

        double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
        ra = re * sf / Math.pow(ra, sn);
        double theta = lon * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);

        grid.put("nx", nx);
        grid.put("ny", ny);
        System.out.println("Converted lat/lon to grid: nx = " + nx + ", ny = " + ny);
        return grid;
    }

    private String getBaseDate() {
        LocalDate now = LocalDate.now();
        if (LocalTime.now().isBefore(LocalTime.of(5, 0))) {
            now = now.minusDays(1);
        }
        return now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    private String getClosestBaseTime() {
        LocalTime now = LocalTime.now();
        List<String> baseTimes = List.of("0200", "0500", "0800", "1100", "1400", "1700", "2000", "2300");
        String closestBaseTime = "2300";
        for (String baseTime : baseTimes) {
            LocalTime time = LocalTime.parse(baseTime, DateTimeFormatter.ofPattern("HHmm"));
            if (now.isAfter(time.plusMinutes(10))) {
                closestBaseTime = baseTime;
            }
        }
        System.out.println("Closest base time determined: " + closestBaseTime);
        return closestBaseTime;
    }

    private Map<String, String> parseTmxTmn(String response) throws Exception {
        Map<String, String> tmxTmnData = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        for (JsonNode item : items) {
            String category = item.path("category").asText();
            String fcstValue = item.path("fcstValue").asText();

            if (category.equals("TMX")) {
                tmxTmnData.put("TMX", fcstValue);
            } else if (category.equals("TMN")) {
                tmxTmnData.put("TMN", fcstValue);
            }
        }
        System.out.println("Parsed TMX/TMN data: " + tmxTmnData);
        return tmxTmnData;
    }

    private Map<String, String> parseRealTimeWeather(String response) throws Exception {
        Map<String, String> realTimeData = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        for (JsonNode item : items) {
            String category = item.path("category").asText();
            String fcstValue = item.path("fcstValue").asText();

            switch (category) {
                case "TMP":
                    realTimeData.put("TMP", fcstValue);
                    break;
                case "SKY":
                    realTimeData.put("SKY", fcstValue);
                    break;
                case "POP":
                    realTimeData.put("POP", fcstValue);
                    break;
                case "WSD":
                    realTimeData.put("WSD", fcstValue);
                    break;
            }
        }
        System.out.println("Parsed real-time weather data: " + realTimeData);
        return realTimeData;
    }
}
