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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/token/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request) {
        return authService.refresh(request);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal MemberPrincipal principal) {
        return authService.getCurrentUser(principal.getId());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest req,
            HttpServletResponse res
    ) {
        String refreshToken = jwtTokenProvider.resolveFromCookie(req, "REFRESH_TOKEN");
        if (!jwtTokenProvider.validateToken(refreshToken)) {
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
        authService.withdrawKakao(res, principal.getId());
        return ResponseEntity.ok().build();
    }
}