package com.duckstar.web.support;

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

import static com.duckstar.util.QuarterUtil.*;

@Slf4j
@Component
public class VoteCookieManager {
    private static final String BASE_VOTE_COOKIE = "vote_cookie_id";

    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.cookie.same-site}")
    private String sameSite;

    /**
     * 정규 정책: 주차명 기반 쿠키 생성 (예: vote_cookie_id_25Q4W2)
     */
    public String ensureVoteCookie(
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw,
            int year,
            int quarter,
            int week
    ) {
        String cookieName = BASE_VOTE_COOKIE + "_" + year + "Q" + quarter + "W" + week;

        String existing = readCookie(requestRaw, year, quarter, week);
        if (existing != null) return existing;

        String cookieId = UUID.randomUUID().toString();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekEnd = getNextWeekEndDate(now);
        Duration ttl = Duration.between(now, weekEnd);

        setCookie(responseRaw, cookieName, cookieId, ttl);
        return cookieId;
    }

    public String readCookie(
            HttpServletRequest req,
            int year,
            int quarter,
            int week
    ) {
        // 예: vote_cookie_id_25Q4W2
        String cookieName = BASE_VOTE_COOKIE + "_" + year + "Q" + quarter + "W" + week;

        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (cookieName.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    private void setCookie(HttpServletResponse res, String name, String value, Duration ttl) {
        ResponseCookie rc = ResponseCookie.from(name, value)
                .httpOnly(false) // 민감정보 아님, 복구용 허용
                .secure(secureCookie)
                .sameSite(sameSite)
                .path("/")
                .maxAge(ttl)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, rc.toString());
    }

    private LocalDateTime getNextWeekEndDate(LocalDateTime time) {
        // nextOrSame 화요일 15시
        LocalDateTime thisTuesDay = time
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.TUESDAY))
                .withHour(15).withMinute(0).withSecond(0).withNano(0);

        // 이미 화요일 15시를 지났다면 → 다음 주 화요일 15시
        if (time.isAfter(thisTuesDay)) {
            thisTuesDay = thisTuesDay.plusWeeks(1);
        }

        return thisTuesDay;
    }

    public String toPrincipalKey(Long memberId, String cookieId) {
        if (memberId != null) return "m:" + memberId;
        else if (cookieId != null && !cookieId.isBlank()) return "c:" + cookieId;
        else return null;
    }
}
