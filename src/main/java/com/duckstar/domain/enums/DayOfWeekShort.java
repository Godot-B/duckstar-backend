package com.duckstar.domain.enums;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public enum DayOfWeekShort {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN,

    SPECIAL;

    private static final DayOfWeekShort[] ENUMS = DayOfWeekShort.values();
    private static final int NIGHT_OFFSET = 5;

    public int getValue() {
        return ordinal() + 1;
    }

    public static LocalDate adjustDateByDayOfWeek(LocalDateTime localDateTime, DayOfWeekShort dayOfWeekShort) {
        int daysDiff = localDateTime.getDayOfWeek().getValue() - dayOfWeekShort.getValue();
        return localDateTime.plusDays(daysDiff).toLocalDate();
    }

    public static DayOfWeekShort of(int dayOfWeek) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new DateTimeException("Invalid value for DayOfWeek: " + dayOfWeek);
        }
        return ENUMS[dayOfWeek - 1];
    }

    public static DayOfWeekShort getLogicalDay(LocalDateTime actualTime) {
        if (actualTime == null) return DayOfWeekShort.SPECIAL;
        LocalDateTime logicalTime = actualTime.minusHours(NIGHT_OFFSET);
        return DayOfWeekShort.of(logicalTime.getDayOfWeek().getValue());
    }
    
    public static DayOfWeekShort getLogicalDay(LocalTime actualTime, DayOfWeekShort actualDayOfWeek) {
        if (actualTime == null) return actualDayOfWeek;
        boolean isMidNight = actualTime.getHour() < 5;
        int value = actualDayOfWeek.getValue();
        DayOfWeekShort yesterDay = value == 1 ? of(7) : of(value - 1);
        return isMidNight ? yesterDay : actualDayOfWeek;
    }
    
    public static int getLogicalHour(LocalTime dateTime) {
        if (dateTime == null) return 99; // 시간 정보 없으면 맨 뒤로
        int hour = dateTime.getHour();
        // 00:00 ~ 04:59 면 24를 더해 24:00 ~ 28:59로 취급
        return (hour < NIGHT_OFFSET) ? hour + 24 : hour;
    }
}