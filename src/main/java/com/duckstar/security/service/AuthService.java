package com.duckstar.security.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.domain.enums.Gender;
import com.duckstar.security.JwtTokenProvider;
import com.duckstar.domain.Member;
import com.duckstar.security.domain.MemberOAuthAccount;
import com.duckstar.security.domain.MemberToken;
import com.duckstar.security.domain.enums.OAuthProvider;
import com.duckstar.security.domain.enums.Role;
import com.duckstar.security.providers.kakao.KakaoAuthClient;
import com.duckstar.security.providers.kakao.KakaoUserResponse;
import com.duckstar.security.repository.MemberOAuthAccountRepository;
import com.duckstar.security.providers.kakao.KakaoApiClient;
import com.duckstar.security.repository.MemberRepository;
import com.duckstar.security.repository.MemberTokenRepository;
import com.duckstar.service.MemberService;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final MemberOAuthAccountRepository memberOAuthAccountRepository;
    private final KakaoApiClient kakaoApiClient;
    private final MemberTokenRepository memberTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoAuthClient kakaoAuthClient;
    private final MemberService memberService;

    @Value("${app.jwt.secure-cookie}")
    private boolean secureCookie;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String clientSecret;

    @Value("${app.kakao.admin-key}")
    private String adminKey;

    private final String sameSite = secureCookie ? "None" : "Lax";

    @Transactional
    public String loginOrRegister(
            String provider,
            String socialAccessToken,
            String socialRefreshToken,
            String jwtRefreshTokenFromCookie,
            HttpServletResponse response
    ) {
        if (!"kakao".equals(provider)) {
            throw new AuthHandler(ErrorStatus.UNSUPPORTED_OAUTH_TYPE);
        }

        KakaoUserResponse userInfo = kakaoApiClient.getUserInfo("Bearer " + socialAccessToken);
        String providerUserId = String.valueOf(userInfo.getId());
        String nickname = (String) userInfo.getProperties().get("nickname");
        String profileImageUrl = (String) userInfo.getProperties().get("profile_image");

        Member member = memberRepository.findByKakaoId(providerUserId)
                .orElseGet(() -> {
                    Member newMember = Member.createKakao(
                            OAuthProvider.KAKAO,
                            providerUserId,
                            nickname,
                            profileImageUrl,
                            null,
                            Gender.NONE,
                            Role.USER
                    );
                    return memberRepository.save(newMember);
                });

        MemberOAuthAccount account = memberOAuthAccountRepository
                .findByProviderAndProviderUserId(OAuthProvider.KAKAO, providerUserId)
                .orElseGet(() -> {
                    MemberOAuthAccount newAccount = MemberOAuthAccount.link(
                            member,
                            OAuthProvider.KAKAO,
                            providerUserId
                    );
                    return memberOAuthAccountRepository.save(newAccount);
                });

        account.setAccessToken(socialAccessToken, LocalDateTime.now().plusHours(6));
        account.setRefreshToken(socialRefreshToken, LocalDateTime.now().plusDays(7));

        Optional<MemberToken> memberTokenOpt = Optional.empty();
        // 기존 JWT refresh token 재사용 가능한지 확인
        if (jwtRefreshTokenFromCookie != null) {
            memberTokenOpt = memberTokenRepository
                    .findByRefreshTokenAndIsValidFalseAndRefreshTokenExpiresAtAfter(
                            jwtRefreshTokenFromCookie, LocalDateTime.now());
        }

        String jwtAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());

        String jwtRefreshToken;
        MemberToken token;
        if (memberTokenOpt.isPresent()) {
            token = memberTokenOpt.get();
            jwtRefreshToken = token.getRefreshToken();
            token.validate();
        } else {
            jwtRefreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole());

            // MemberToken 저장
            token = MemberToken.create(
                    member,
                    jwtRefreshToken,
                    LocalDateTime.now().plusDays(7)
            );
            memberTokenRepository.save(token);
        }

        // 쿠키 내려주기
        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", jwtAccessToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofHours(1))
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", jwtRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        
        // JWT access token 반환
        return jwtAccessToken;
    }

    public ResponseEntity<Map<String, Object>> getCurrentUser(Long principalId) {
        Member member = memberRepository.findById(principalId)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND));

        Map<String, Object> userInfo = Map.of(
                "id", member.getId(),
                "nickname", member.getNickname(),
                "profileImageUrl", member.getProfileImageUrl(),
                "role", member.getRole().name()
        );

        return ResponseEntity.ok(userInfo);
    }

    @Transactional
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveFromCookie(request, "REFRESH_TOKEN");
        boolean tokenIsValid = jwtTokenProvider.validateToken(refreshToken);
        if (!tokenIsValid) {
            throw new AuthHandler(ErrorStatus.INVALID_TOKEN);
        }

        MemberToken memberToken = memberTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.MEMBER_TOKEN_NOT_FOUND));

        if (!memberToken.isValid()) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_REUSED);
        }

        if (memberToken.isExpired()) {
            throw new AuthHandler(ErrorStatus.REFRESH_TOKEN_EXPIRED);
        }

        Long memberId = memberToken.getMember().getId();
        Member member = memberService.findByIdOrThrow(memberId);

        String jwtAccessToken = jwtTokenProvider.createAccessToken(memberId, member.getRole());
        String jwtRefreshToken = jwtTokenProvider.createRefreshToken(memberId, member.getRole());

        // 회전: 기존 토큰 삭제 + 새 토큰 저장
        memberTokenRepository.delete(memberToken);
        memberTokenRepository.save(
                MemberToken.create(
                        member,
                        jwtRefreshToken,
                        LocalDateTime.now().plusDays(7)
                )
        );

        /**
         * 프로파일별 분기
         * local: Secure=false, SameSite=Lax (또는 None + CORS 고려)
         * prod(HTTPS): Secure=true, SameSite=None (SPA 크로스사이트 XHR 고려 시)
         */

        ResponseCookie accessCookie = ResponseCookie.from("ACCESS_TOKEN", jwtAccessToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofHours(1)).build();

        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", jwtRefreshToken)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(Duration.ofDays(7)).build();

        Map<String, String> tokenResponse = Map.of(
                "accessToken", jwtAccessToken,
                "refreshToken", jwtRefreshToken
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(tokenResponse);
    }

    @Transactional
    public void logout(HttpServletResponse res, String refreshToken) {
        MemberToken memberToken = memberTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.MEMBER_TOKEN_NOT_FOUND));
        memberToken.invalidate();

        expireCookie(res, "ACCESS_TOKEN");
    }

    private void expireCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Transactional
    public void withdrawKakao(HttpServletResponse res, Long principalId) {
        Member member = memberRepository.findById(principalId)
                .orElseThrow(() -> new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND));

        MemberOAuthAccount account =
                memberOAuthAccountRepository.findByProviderAndMemberId(OAuthProvider.KAKAO, principalId)
                        .orElseThrow(() -> new AuthHandler(ErrorStatus.OAUTH_ACCOUNT_NOT_FOUND));

        boolean accessTokenHasExpired = account.getAccessTokenExpiresAt().isBefore(LocalDateTime.now());
        boolean refreshTokenHasExpired = account.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now());

        String accessToken = account.getAccessToken();

        if (accessTokenHasExpired && refreshTokenHasExpired) {
            try {
                kakaoApiClient.unlink("KakaoAK " + adminKey);
            } catch (FeignException e) {
                log.warn("카카오 unlink 실패 - principalId={}, 이유={}", principalId, e.getMessage());
            }
        } else {
            if (!refreshTokenHasExpired) {
                accessToken = kakaoAuthClient.getTokenWithRefreshToken(
                        "refresh_token",
                        clientId,
                        account.getRefreshToken(),
                        clientSecret
                ).getAccessToken();
            }

            try {
                kakaoApiClient.unlink("Bearer " + accessToken);
            } catch (FeignException e) {
                log.warn("카카오 unlink 실패 - principalId={}, 이유={}", principalId, e.getMessage());
            }
        }

        expireCookie(res, "ACCESS_TOKEN");
        expireCookie(res, "REFRESH_TOKEN");
        memberTokenRepository.deleteAllByMemberId(principalId);
        memberOAuthAccountRepository.deleteAllByMemberId(principalId);
        member.withdraw();
    }
}
