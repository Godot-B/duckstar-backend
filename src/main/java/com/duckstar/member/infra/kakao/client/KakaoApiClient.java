package com.duckstar.member.infra.kakao.client;

import com.duckstar.member.infra.kakao.dto.KakaoMemberReponse;
import com.duckstar.member.infra.kakao.dto.KakaoToken;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface KakaoApiClient {
    @PostExchange(url = "https://kauth.kakao.com/oauth/token",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    KakaoToken fetchToken(@RequestParam MultiValueMap<String, String> params);

    @GetExchange("https://kapi.kakao.com/v2/user/me")
    KakaoMemberReponse fetchMember(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken);
}
