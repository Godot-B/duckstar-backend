package com.duckstar.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

public class QuarterUtil {
    private static final int ANCHOR_HOUR = 18;

    public record YQWRecord(int yearValue, int quarterValue, int weekValue) {}
    public record AnchorInfo(int year, int quarter, LocalDateTime anchorStart) {}

    // 헬퍼 메서드
    private static int calendarQuarter(int month) { return ((month - 1) / 3) + 1; }
    private static LocalDate firstDayOfQuarter(int year, int quarter) {
        int month = (quarter - 1) * 3 + 1; // 1,4,7,10
        return LocalDate.of(year, month, 1);
    }

    /** 분기 앵커: 분기 첫날이 포함된 주의 월요일 18:00 (첫날 이전 또는 같은 월요일) */
    private static LocalDateTime anchorForQuarter(int year, int quarter) {
        LocalDate first = firstDayOfQuarter(year, quarter); // 1,4,7,10월 1일
        LocalDate anchorDate = first.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return LocalDateTime.of(anchorDate, LocalTime.of(ANCHOR_HOUR, 0));
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

        if (time.isBefore(currAnchor)) {
            int prevQ = (q == 1) ? 4 : q - 1;
            int prevY = (q == 1) ? y - 1 : y;
            LocalDateTime prevAnchor = anchorForQuarter(prevY, prevQ);

            return new AnchorInfo(prevY, prevQ, prevAnchor);

        } else if (time.isBefore(nextAnchor)) {
            return new AnchorInfo(y, q, currAnchor);
        } else {
            return new AnchorInfo(nextY, nextQ, nextAnchor);
        }
    }

    /** 분기 주차: 앵커 기준 7일(=168시간) 단위, 1부터 시작 */
    private static int weekOfQuarter(LocalDateTime time, LocalDateTime anchor) {
        long hours = ChronoUnit.HOURS.between(anchor, time);
        return (int)(hours / (7 * 24)) + 1; // 168시간 단위
    }

    //===== Public API =====
    public static YQWRecord getThisWeekRecord(LocalDateTime time) {
        AnchorInfo ai = resolveAnchor(time);
        int week = weekOfQuarter(time, ai.anchorStart());
        return new YQWRecord(ai.year(), ai.quarter(), week);
    }

    /**
     * 입력된 time이 속한 '비즈니스 주차'의 시작 월요일의 ANCHOR_HOUR를 반환
     */
    public static LocalDateTime getThisWeekStartedAt(LocalDateTime time) {
        // 예: 18:00 이전이면 전날로 판정되게끔 18시간 빼기
        LocalDate businessDate = time.minusHours(ANCHOR_HOUR).toLocalDate();
        LocalDate monday = businessDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return LocalDateTime.of(monday, LocalTime.of(18, 0));
    }
}