package com.duckstar.security.controller;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.security.jwt.JwtTokenProvider;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.service.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpleRateLimiter rateLimiter;

    @Operation(summary = "Refresh Token 재발급 API")
    @PostMapping("/token/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        // refresh token 파싱 → memberId 얻기
        String refreshToken = jwtTokenProvider.resolveFromCookie(request, "REFRESH_TOKEN");
        Long memberId = jwtTokenProvider.extractMemberId(refreshToken);

        if (!rateLimiter.isAllowedByIp(clientIp, 10, Duration.ofMinutes(1)) ||
                !rateLimiter.isAllowedByUser(memberId, 10, Duration.ofMinutes(1))) {
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        return authService.refresh(request);
    }

    @Operation(summary = "로그아웃 API")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();

        if (!rateLimiter.isAllowedByIp(clientIp, 30, Duration.ofMinutes(1))) {
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "카카오 회원 탈퇴 API")
    @PostMapping("/withdraw/kakao")
    public ResponseEntity<Void> withdrawKakao(
            HttpServletResponse response,
            @AuthenticationPrincipal MemberPrincipal principal) {

        if (principal == null)
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);

        Long memberId = principal.getId();
        if (!rateLimiter.isAllowedByUser(memberId, 10, Duration.ofMinutes(1))) {
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        authService.withdrawKakao(response, memberId);
        return ResponseEntity.ok().build();
    }
}