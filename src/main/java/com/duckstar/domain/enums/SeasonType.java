package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.MonthDay;
import java.util.Date;

@Getter
@RequiredArgsConstructor
public enum SeasonType {

    SPRING(1, MonthDay.of(3, 21)),
    SUMMER(2, MonthDay.of(6, 21)),
    AUTUMN(3, MonthDay.of(9, 23)),
    WINTER(4, MonthDay.of(12, 22));

    private final int order;
    private final MonthDay startDate;
}
