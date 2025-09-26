package com.duckstar.security.oauth2;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.domain.Member;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.domain.MemberToken;
import com.duckstar.security.jwt.JwtTokenProvider;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.repository.MemberTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberTokenRepository memberTokenRepository;

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        // 1. Principal 추출
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Long memberId = principal.getId();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.MEMBER_NOT_FOUND));

        // 2. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(memberId, member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId, member.getRole());

        // 3. Refresh Token 저장 (회전 고려)
        memberTokenRepository.save(
                MemberToken.create(
                        member,
                        refreshToken,
                        LocalDateTime.now().plusDays(1)
                )
        );

        // 4. 쿠키 설정 (SPA & 보안 설정)
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", accessToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 5. JSON Body 응답 (Swagger / 모바일 앱 디버깅 편의용)
        Map<String, String> tokenResponse = Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getWriter(), tokenResponse);

        log.info("✅ 로그인 성공 - memberId={}, role={}", memberId, member.getRole());
    }
}
