package com.duckstar.member.infra.kakao.dto;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.*;

@JsonNaming(SnakeCaseStrategy.class)
public record KakaoToken(
        String tokenType,
        String accessToken,
        String idToken,
        Integer expiresIn,
        String refreshToken,
        Integer refreshTokenExpiresIn,
        String scope
) {
}
