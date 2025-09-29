package com.duckstar.security.oauth2;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.domain.Member;
import com.duckstar.repository.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.jwt.JwtTokenProvider;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.service.AuthService;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;

    private final AuthService authService;

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtTokenProvider jwtTokenProvider;
    private final VoteCookieManager voteCookieManager;

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    @Value("${app.base-url}")
    private String baseUrl;

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

        // *** NAVER, GOOGLE 은 회원 탈퇴를 위해 소셜 리프레시 토큰 저장 ***
        if (member.getProvider() == OAuthProvider.NAVER ||
                member.getProvider() == OAuthProvider.GOOGLE) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            // 👉 여기서 AuthorizedClient 를 꺼내면 소셜 리프레시 토큰이 들어있음
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(),
                    oauthToken.getName()
            );

            if (client != null && client.getRefreshToken() != null &&
                member.getSocialRefreshToken() == null) {  // 이미 저장된 소셜 토큰 없을 때만

                String refreshToken = client.getRefreshToken().getTokenValue();
                Instant expiresAt = client.getRefreshToken().getExpiresAt();

                LocalDateTime socialExpiresAt = expiresAt != null
                        ? LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault())
                        : null;

                // DB에 저장
                member.setSocialRefreshToken(
                        refreshToken,
                        socialExpiresAt
                );
                memberRepository.save(member);
            }
        }

        // 2. JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(memberId, member.getRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId, member.getRole());

        // 3-1. 토큰 회전, 비회원 투표 마이그레이션
        boolean isMigrated = authService.saveTokenAndMigrateVote(request, response, member, refreshToken);

        // 3-2. UX 쿠키 보조 세팅
        if (weekVoteSubmissionRepository.existsByMember_Id(memberId)) {
            voteCookieManager.markVotedThisWeek(request, response);
        }

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

        boolean isNewUser = !member.getProfileInitialized();

        // LOGIN_STATE 임시 쿠키, 로그인 전 페이지 복원용
        String loginStateJson = String.format("{\"isNewUser\":%s,\"isMigrated\":%s}", isNewUser, isMigrated);
        String encoded = Base64.getUrlEncoder().encodeToString(loginStateJson.getBytes(StandardCharsets.UTF_8));

        ResponseCookie stateCookie = ResponseCookie.from("LOGIN_STATE", encoded)
                .httpOnly(false) // 프론트 JS 접근 가능
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofMinutes(1)) // 1분 정도만 유지
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, stateCookie.toString());

        response.sendRedirect(baseUrl); // "/login/oauth2/code/kakao" 등 그대로 두지 않도록 안전 redirect

        log.info("✅ 로그인 성공 - memberId={}, role={}", memberId, member.getRole());
    }
}
