package com.duckstar.web.support;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.VoteHandler;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

@Slf4j
@Component
public class VoteCookieManager {
    private static final String VOTE_COOKIE = "vote_cookie_id";
    private static final String VOTED_THIS_WEEK = "voted_this_week";

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site}")
    private String sameSite;

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
        if (existing != null && !existing.isBlank()) return existing;

        String cookieId = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekEnd = getNextWeekEndDate(now);
        long ttlSec = Duration.between(now, weekEnd).getSeconds();

        ResponseCookie cookie = ResponseCookie.from(VOTE_COOKIE, cookieId)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(ttlSec)
                .build();

        responseRaw.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return cookieId;
    }

    public void markVotedThisWeek(HttpServletRequest requestRaw, HttpServletResponse responseRaw) {
        String existing = null;
        if (requestRaw.getCookies() != null) {
            for (Cookie cookie : requestRaw.getCookies()) {
                if (VOTED_THIS_WEEK.equals(cookie.getName())) {
                    existing = cookie.getValue();
                    break;
                }
            }
        }
        if (existing != null && !existing.isBlank()) {
            // 이미 존재하는 경우 스킵
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekEnd = getNextWeekEndDate(now);
        long ttlSec = Duration.between(now, weekEnd).getSeconds();

        String flagId = "1";

        ResponseCookie flag = ResponseCookie.from(VOTED_THIS_WEEK, flagId)
                .httpOnly(false)
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(ttlSec)
                .build();

        responseRaw.addHeader(HttpHeaders.SET_COOKIE, flag.toString());
    }

    private LocalDateTime getNextWeekEndDate(LocalDateTime time) {
        // 이번 주 금요일 19시
        LocalDateTime thisFriday = time
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .withHour(19).withMinute(0).withSecond(0).withNano(0);

        // 이미 금요일 19시를 지났다면 → 다음 주 금요일 19시
        if (time.isAfter(thisFriday)) {
            thisFriday = thisFriday.plusWeeks(1);
        }

        return thisFriday;
    }

    public String toPrincipalKey(Long memberId, String cookieId) {
        if (memberId != null) return "m:" + memberId;
        else if (cookieId != null && !cookieId.isBlank()) return "c:" + cookieId;
        else return null;
    }
}
