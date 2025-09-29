package com.duckstar.web.dto;

import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.VoteStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class WeekResponseDto {

    @Builder
    @Getter
    public static class WeekDto {
        VoteStatus voteStatus;

        Integer year;

        Integer quarter;

        Integer week;

        LocalDate startDate;

        LocalDate endDate;

        public static WeekDto of(Week week) {
            Quarter quarter = week.getQuarter();
            return WeekDto.builder()
                    .voteStatus(week.getStatus())
                    .year(quarter.getYearValue())
                    .quarter(quarter.getQuarterValue())
                    .week(week.getWeekValue())
                    .startDate(week.getStartDateTime().toLocalDate())
                    .endDate(week.getEndDateTime().toLocalDate())
                    .build();
        }
    }
}
