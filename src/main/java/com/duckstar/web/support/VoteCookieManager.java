package com.duckstar.web.support;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VoteCookieManager {
    private static final String VOTE_COOKIE = "vote_cookie_id";
    private static final long VOTE_COOKIE_TTL_SEC = 60L * 60 * 24 * 180;  // 180일

    public String ensureVoteCookie(HttpServletRequest requestRaw, HttpServletResponse responseRaw) {
        String existing = null;
        if (requestRaw.getCookies() != null) {
            for (Cookie cookie : requestRaw.getCookies()) {
                if (VOTE_COOKIE.equals(cookie.getName())) {
                    existing = cookie.getValue();
                    break;
                }
            }
        }
        if (existing != null && existing.isBlank()) return existing;

        String cookieId = UUID.randomUUID().toString();
        ResponseCookie cookie = ResponseCookie.from(VOTE_COOKIE, cookieId)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(VOTE_COOKIE_TTL_SEC)
                .build();

        responseRaw.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return cookieId;
    }

    public String toPrincipalKey(Long memberId, String cookieId) {
        if (memberId != null) return "m:" + memberId;
        if (cookieId != null && !cookieId.isBlank()) return "c:" + cookieId;
        throw new VoteHandler(ErrorStatus.VOTE_AUTH_REQUIRED);
    }
}
