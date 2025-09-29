package com.duckstar.security.oauth2;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.time.Instant;
import java.util.Map;

public class CustomOAuth2AccessTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {

    private final DefaultMapOAuth2AccessTokenResponseConverter delegate =
            new DefaultMapOAuth2AccessTokenResponseConverter();

    @Override
    public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
        OAuth2AccessTokenResponse response = delegate.convert(tokenResponseParameters);

        String refreshToken = (String) tokenResponseParameters.get("refresh_token");

        OAuth2AccessTokenResponse.Builder builder = OAuth2AccessTokenResponse
                .withToken(response.getAccessToken().getTokenValue())
                .tokenType(response.getAccessToken().getTokenType())
                .scopes(response.getAccessToken().getScopes());

        if (response.getAccessToken().getExpiresAt() != null) {
            long expiresIn = response.getAccessToken().getExpiresAt().getEpochSecond()
                    - Instant.now().getEpochSecond();
            builder.expiresIn(expiresIn);
        }

        if (refreshToken != null) {
            builder.refreshToken(refreshToken);
        }

        builder.additionalParameters(response.getAdditionalParameters());

        return builder.build();
    }
}
