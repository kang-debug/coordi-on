package com.fivefull.coordiback.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoService {

    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect_uri}")
    private String redirectUri;

    public Map<String, String> getKakaoUserInfo(String code) {
        RestTemplate restTemplate = new RestTemplate();

        try {
            String tokenUrl = "https://kauth.kakao.com/oauth/token" +
                    "?grant_type=authorization_code" +
                    "&client_id=" + clientId +
                    "&redirect_uri=" + redirectUri +
                    "&code=" + code;

            Map<String, String> tokenResponse = restTemplate.postForObject(tokenUrl, null, Map.class);
            if (tokenResponse == null || !tokenResponse.containsKey("access_token")) {
                throw new RuntimeException("Access token retrieval failed.");
            }
            String accessToken = tokenResponse.get("access_token");

            String infoUrl = "https://kapi.kakao.com/v2/user/me";
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            Map<String, Object> kakaoResponse = restTemplate.exchange(infoUrl, HttpMethod.GET, entity, Map.class).getBody();

            Map<String, String> userInfo = new HashMap<>();
            if (kakaoResponse == null) {
                throw new RuntimeException("Failed to retrieve user information from Kakao.");
            }

            Map<String, Object> kakaoAccount = (Map<String, Object>) kakaoResponse.get("kakao_account");
            if (kakaoAccount == null || !kakaoAccount.containsKey("email")) {
                throw new RuntimeException("Failed to retrieve email information from Kakao account.");
            }
            userInfo.put("email", kakaoAccount.get("email").toString());
            userInfo.put("socialId", kakaoResponse.get("id").toString());

            return userInfo;

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST && e.getResponseBodyAsString().contains("invalid_grant")) {
                throw new RuntimeException("Authorization code expired or invalid. Please retry login.");
            } else {
                throw new RuntimeException("Kakao API request failed: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
            }
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while processing Kakao user information.", e);
        }
    }

}
