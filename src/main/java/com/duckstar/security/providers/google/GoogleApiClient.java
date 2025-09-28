package com.duckstar.security.providers.google;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "google-api", url = "https://oauth2.googleapis.com")
public interface GoogleApiClient {
    @PostMapping(value = "/revoke", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void revoke(@RequestParam("token") String token,
                @RequestParam("token_type_hint") String type);

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    GoogleTokenResponse exchangeCode(@RequestBody GoogleTokenRequest request);
}
