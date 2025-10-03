package com.duckstar.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.*;

public class QuarterUtil {

    public record YQWRecord(int yearValue, int quarterValue, int weekValue) {}
    public record AnchorInfo(int year, int quarter, LocalDateTime anchorStart) {}

    // ---- helpers ----
    private static int calendarQuarter(int month) { return ((month - 1) / 3) + 1; }
    private static LocalDate firstDayOfQuarter(int year, int quarter) {
        int month = (quarter - 1) * 3 + 1; // 1,4,7,10
        return LocalDate.of(year, month, 1);
    }

    /** 분기 앵커: 첫날이 토 → 이전 금 19:00, 그 외 → 다음(또는 같은) 금 19:00 */
    private static LocalDateTime anchorForQuarter(int year, int quarter) {
        LocalDate first = firstDayOfQuarter(year, quarter);
        DayOfWeek dow = first.getDayOfWeek();

        LocalDate anchorDate = (dow == SATURDAY)
                ? first.with(TemporalAdjusters.previous(FRIDAY))      // 토요일이면 전날 금요일
                : first.with(TemporalAdjusters.nextOrSame(FRIDAY));   // 그 외는 다음/같은 금요일

        return LocalDateTime.of(anchorDate, LocalTime.of(19, 0));
    }

    /**
     * time 이 속한 "비즈니스 분기"의 (연도, 분기, 앵커 시작시각)을 판정
     * 구간 정의: [anchor(y,q), anchor(y',q'))  (좌측 포함, 우측 배타)
     */
    public static AnchorInfo resolveAnchor(LocalDateTime time) {
        int y = time.getYear();
        int q = calendarQuarter(time.getMonthValue());

        LocalDateTime currAnchor = anchorForQuarter(y, q);
        int nextQ = (q == 4) ? 1 : q + 1;
        int nextY = (q == 4) ? y + 1 : y;
        LocalDateTime nextAnchor = anchorForQuarter(nextY, nextQ);

        if (!time.isBefore(nextAnchor)) { // time >= nextAnchor → 다음 분기
            return new AnchorInfo(nextY, nextQ, nextAnchor);
        }
        if (time.isBefore(currAnchor)) {  // time < 현재 분기 앵커 → 이전 분기
            int prevQ = (q == 1) ? 4 : q - 1;
            int prevY = (q == 1) ? y - 1 : y;
            LocalDateTime prevAnchor = anchorForQuarter(prevY, prevQ);
            return new AnchorInfo(prevY, prevQ, prevAnchor);
        }
        return new AnchorInfo(y, q, currAnchor); // currAnchor ≤ time < nextAnchor
    }

    /** 분기 주차: 앵커 기준 7일 단위, 1부터 시작 */
    private static int weekOfQuarter(LocalDateTime time, LocalDateTime anchor) {
        long days = ChronoUnit.DAYS.between(anchor, time);
        return (int)(days / 7) + 1;
    }

    // ---- Public API ----
    public static YQWRecord getThisWeekRecord(LocalDateTime time) {
        AnchorInfo ai = resolveAnchor(time);
        int week = weekOfQuarter(time, ai.anchorStart());
        // 분기 "연도"는 항상 firstDayOfQuarter의 연도로 정의 (앵커가 전월일 수 있으므로 주의)
        int quarterYear = firstDayOfQuarter(ai.year(), ai.quarter()).getYear();
        return new YQWRecord(quarterYear, ai.quarter(), week);
    }
}