package com.duckstar.util;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.WeekHandler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import static java.time.DayOfWeek.*;

public class QuarterUtil {

    public record YQWRecord(
            int yearValue,
            int quarterValue,
            int weekValue
    )
    {}

    private static LocalDate getStartDateOfQuarter(LocalDate date) {
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

    // 특정 날짜의 분기 값 (1~4)
    private static int getQuarterValue(LocalDate date) {
        LocalDate quarterStart = getStartDateOfQuarter(date);
        return ((quarterStart.getMonthValue() - 1) / 3) + 1;
    }

    // 비즈니스 규칙에 맞는 연도, 분기, 주차 계산
    public static YQWRecord getThisWeekRecord(LocalDateTime time) {
        LocalDate weekStartDate;
        LocalDate weekEndDate;
        if (!time.getDayOfWeek().equals(SUNDAY) || time.getHour() < 22) {
            throw new WeekHandler(ErrorStatus.ROTATION_POLICY_VIOLATION);
        }

        // 주 시작(일요일)과 종료(토요일)
        // 여기서는 분기 변경 계산을 위해 종료일을 토요일로 간주
        weekStartDate = time.toLocalDate();
        weekEndDate = weekStartDate.plusDays(6);

        int startQuarterValue = getQuarterValue(weekStartDate);
        int endQuarterValue = getQuarterValue(weekEndDate);

        // 분기 변경 주
        if (startQuarterValue != endQuarterValue) {
            // 경계일 요일 판단
            LocalDate borderDate = getStartDateOfQuarter(weekEndDate);
            if (isEarlyInWeek(borderDate.getDayOfWeek())) {
                return new YQWRecord(weekEndDate.getYear(), endQuarterValue, 1);
            }
        }

        return new YQWRecord(
                weekStartDate.getYear(),
                startQuarterValue,
                getWeekNumberOf(weekStartDate)
        );
    }

    private static int getWeekNumberOf(LocalDate weekStartDate) {
        LocalDate quarterStartDate = getStartDateOfQuarter(weekStartDate);
        LocalDate lastSunday = quarterStartDate.with(TemporalAdjusters.previousOrSame(SUNDAY));
        return isEarlyInWeek(quarterStartDate.getDayOfWeek()) ?
                (int) ChronoUnit.WEEKS.between(lastSunday, weekStartDate) + 1 :
                (int) ChronoUnit.WEEKS.between(lastSunday, weekStartDate);
    }

    private static boolean isEarlyInWeek(DayOfWeek day) {
        return day == SUNDAY || day == MONDAY || day == TUESDAY || day == WEDNESDAY;
    }
}