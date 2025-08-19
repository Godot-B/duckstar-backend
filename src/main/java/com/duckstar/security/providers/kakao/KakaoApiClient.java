package com.duckstar.security.providers.kakao;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakao-api", url = "https://kapi.kakao.com")
public interface KakaoApiClient {

    @GetMapping(value = "/v2/user/me", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoUserResponse getUserInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization);

    @PostMapping(value = "/v1/user/unlink", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void unlink(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);
}
