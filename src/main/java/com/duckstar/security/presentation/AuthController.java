package com.duckstar.security.presentation;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AuthHandler;
import com.duckstar.security.JwtTokenProvider;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SimpleRateLimiter rateLimiter;

    @PostMapping("/token/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {
        String clientIp = request.getRemoteAddr();

        if (!rateLimiter.isAllowedByIp(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        String clientIp = req.getRemoteAddr();

        if (!rateLimiter.isAllowedByIp(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
        }

        String refreshToken = jwtTokenProvider.resolveFromCookie(req, "REFRESH_TOKEN");
        boolean tokenIsValid = jwtTokenProvider.validateToken(refreshToken);
        if (!tokenIsValid) {
            throw new AuthHandler(ErrorStatus.INVALID_TOKEN);
        }

        authService.logout(res, refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw/kakao")
    public ResponseEntity<Void> withdrawKakao(
            HttpServletResponse res,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        if (principal != null) {
            // 사용자별 Rate Limiting
            if (!rateLimiter.isAllowedByUser(principal.getId())) {
                log.warn("Rate limit exceeded for user: {}", principal.getId());
                throw new AuthHandler(ErrorStatus.TOO_MANY_REQUESTS);
            }
        }

        authService.withdrawKakao(res, principal);
        return ResponseEntity.ok().build();
    }
}