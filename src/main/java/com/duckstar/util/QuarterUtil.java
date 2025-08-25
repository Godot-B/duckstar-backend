package com.duckstar.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class QuarterUtil {

    public static LocalDate getStartDateOfQuarter(LocalDate date) {
        int monthValue = date.getMonthValue();
        int month = 0;
        switch (monthValue) {
            case 1, 2, 3 -> month = 1;
            case 4, 5, 6 -> month = 4;
            case 7, 8, 9 -> month = 7;
            case 10, 11, 12 -> month = 10;
        }
        return LocalDate.of(date.getYear(), month, 1);
    }

    public static LocalDate getEndDateOfQuarter(LocalDate date) {
        LocalDate start = getStartDateOfQuarter(date);
        return start.plusMonths(3).minusDays(1);
    }

    // 특정 날짜의 분기 값 (1~4)
    public static int getQuarterValue(LocalDate date) {
        return ((getStartDateOfQuarter(date).getMonthValue() - 1) / 3) + 1;
    }


    // 비즈니스 규칙에 맞는 분기 계산
    public static int getBusinessQuarter(LocalDate date) {
        // 주 시작(일요일)과 종료(토요일)
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() % 7);
        LocalDate weekEnd = weekStart.plusDays(6);

        int startQuarter = getQuarterValue(weekStart);
        int endQuarter = getQuarterValue(weekEnd);

        // 같은 분기면 바로 반환
        if (startQuarter == endQuarter) {
            return startQuarter;
        }

        // 분기 경계에 걸친 주 → 각 분기에 며칠씩 포함되는지 비교
        LocalDate startQuarterEnd = getEndDateOfQuarter(weekStart);
        long startQuarterDays = ChronoUnit.DAYS.between(weekStart, startQuarterEnd) + 1;

        LocalDate endQuarterStart = getStartDateOfQuarter(weekEnd);
        long endQuarterDays = ChronoUnit.DAYS.between(endQuarterStart, weekEnd) + 1;

        // 날짜가 더 많은 분기를 선택
        return startQuarterDays >= endQuarterDays ? startQuarter : endQuarter;
    }

    // 비즈니스 규칙에 따른 분기 주차 계산
    public static int calculateBusinessWeekNumber(LocalDate date) {
        int quarter = getBusinessQuarter(date);

        // 분기 시작일 및 첫 번째 일요일 계산
        LocalDate quarterStart = LocalDate.of(date.getYear(), (quarter - 1) * 3 + 1, 1);
        LocalDate firstSunday = quarterStart.minusDays(quarterStart.getDayOfWeek().getValue() % 7);

        // 주 시작일
        LocalDate weekStart = date.minusDays(date.getDayOfWeek().getValue() % 7);

        // 주차 계산
        long weeks = ChronoUnit.WEEKS.between(firstSunday, weekStart) + 1;
        return (int) weeks;
    }
}