package com.duckstar.security.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.domain.Member;
import com.duckstar.security.jwt.JwtTokenProvider;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.service.AuthService;
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

    @Operation(summary = "로그아웃 API", description = "로그아웃 시, 회원이 투표한 후보들 중" +
            " 가장 마지막에 투표가 마감되는 후보에 대해 투표의 남은 시간만큼 프론트에 반환:" +
            " 프론트는 해당 시간만큼 중복 투표 방지 화면을 띄움.")
    @PostMapping("/logout")
    public ApiResponse<Long> logout(HttpServletRequest request, HttpServletResponse response) {
        String clientIp = request.getRemoteAddr();

        if (!rateLimiter.isAllowedByIp(clientIp, 30, Duration.ofMinutes(1))) {
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        return ApiResponse.onSuccess(authService.logout(request, response));
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

    @Operation(summary = "회원탈퇴 모드 설정 API")
    @PostMapping("/set-withdraw-mode")
    public ResponseEntity<Void> setWithdrawMode(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request
    ) {
        Boolean withdrawMode = (Boolean) body.get("withdrawMode");
        String provider = (String) body.get("provider");
        
        System.out.println("🔧 회원탈퇴 모드 설정 API 호출됨 - withdrawMode=" + withdrawMode + ", provider=" + provider);
        
        if (withdrawMode != null && withdrawMode) {
            request.getSession().setAttribute("withdrawMode", "true");
            request.getSession().setAttribute("withdrawProvider", provider);
            System.out.println("✅ 세션에 회원탈퇴 모드 저장됨");
        }
        
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "구글 회원 탈퇴 API")
    @PostMapping("/withdraw/google")
    public ResponseEntity<Void> withdrawGoogle(
            HttpServletResponse response,
            @AuthenticationPrincipal MemberPrincipal principal) {
        if (principal == null)
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);

        Long memberId = principal.getId();
        if (!rateLimiter.isAllowedByUser(memberId, 10, Duration.ofMinutes(1))) {
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        authService.withdrawGoogle(response, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "네이버 회원 탈퇴 API")
    @PostMapping("/withdraw/naver")
    public ResponseEntity<Void> withdrawNaver(
            HttpServletResponse response,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        if (principal == null)
            throw new AuthHandler(ErrorStatus.PRINCIPAL_NOT_FOUND);

        Long memberId = principal.getId();
        if (!rateLimiter.isAllowedByUser(memberId, 10, Duration.ofMinutes(1))) {
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        authService.withdrawNaver(response, memberId);
        return ResponseEntity.ok().build();
    }
}