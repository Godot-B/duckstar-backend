package com.duckstar.member.infra.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.kakao")
public record KakaoOauthConfig(
        String redirectUrI,
        String clientId,
        String clientSecret,
        String[] scope
) {
}
