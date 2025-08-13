package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.MedalDto.*;

public class WeekResponseDto {

    @Getter
    @Builder
    public static class WeekDataDto {
        List<AllKindsWeekDto> allKindsWeekDtos;

        List<RackUnitDto> rackUnitDtos;
    }

    @Builder
    @Getter
    public static class AllKindsWeekDto {
        Boolean isBreak;

        Integer quarter;

        Integer week;

        LocalDateTime airDateTime;

        LocalDateTime airDateTimePlusWeek;
    }

    @Builder
    @Getter
    public static class WeekDto {
        Integer year;

        Integer quarter;

        Integer week;

        LocalDate startDate;

        LocalDate endDate;
    }
}
