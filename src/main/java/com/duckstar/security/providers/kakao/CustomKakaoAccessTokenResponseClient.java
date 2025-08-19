package com.duckstar.security.providers.kakao;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomKakaoAccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final KakaoAuthClient kakaoAuthClient;

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        ClientRegistration registration = request.getClientRegistration();
        String code = request.getAuthorizationExchange().getAuthorizationResponse().getCode();

        KakaoTokenResponse tokenResponse = kakaoAuthClient.getTokenWhenLogIn(
                "authorization_code",
                registration.getClientId(),
                registration.getRedirectUri(),
                code,
                registration.getClientSecret()
        );

        return OAuth2AccessTokenResponse.withToken(tokenResponse.getAccessToken())
                .expiresIn(tokenResponse.getExpiresIn())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .build();
    }
}
