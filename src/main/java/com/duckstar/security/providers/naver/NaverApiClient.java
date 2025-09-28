package com.duckstar.security.providers.naver;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "naver-api", url = "https://nid.naver.com")
public interface NaverApiClient {
    @PostMapping(value = "/oauth2.0/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    NaverTokenResponse exchangeCode(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("code") String code,
            @RequestParam("state") String state
    );

    @GetMapping("/oauth2.0/token")
    Map<String, Object> deleteToken(
            @RequestParam("grant_type") String grantType,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("access_token") String accessToken,
            @RequestParam("service_provider") String serviceProvider
    );
}
