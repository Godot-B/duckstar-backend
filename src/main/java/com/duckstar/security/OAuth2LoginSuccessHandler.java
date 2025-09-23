package com.duckstar.security;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static com.duckstar.security.service.AuthService.*;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest req,
            HttpServletResponse res,
            Authentication authentication
    ) throws IOException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;

        // access token을 꺼내려면 client에서 꺼내야 함
        String provider = token.getAuthorizedClientRegistrationId();
        OAuth2AuthorizedClient authorizedClient =
                authorizedClientService.loadAuthorizedClient(
                        provider, token.getName());

        String socialAccessToken = authorizedClient.getAccessToken().getTokenValue();
        String socialRefreshToken = authorizedClient.getRefreshToken() != null ?
                authorizedClient.getRefreshToken().getTokenValue() :
                null;

        String jwtRefreshTokenFromCookie = jwtTokenProvider.resolveFromCookie(req, "REFRESH_TOKEN");

        // JWT 토큰 생성 및 쿠키 설정
        LoginResponseDto loginResponse = authService.loginOrRegister(
                        provider,
                        socialAccessToken,
                        socialRefreshToken,
                        jwtRefreshTokenFromCookie,
                        res
                );

        // 프론트엔드로 리다이렉트 시 JWT access token 및 플래그를 쿼리 파라미터로 전달
        String redirectUrl = frontendRedirectUri +
                "?accessToken=" +
                URLEncoder.encode(loginResponse.jwtAccessToken(), StandardCharsets.UTF_8) +
                "&isNewUser=" + loginResponse.isNewUser();

        res.sendRedirect(redirectUrl);
    }
}