package com.duckstar.web.dto;

import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
import com.duckstar.util.QuarterUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

import static com.duckstar.util.QuarterUtil.*;

public class WeekResponseDto {

    @Builder
    @Getter
    public static class WeekDto {
        Integer year;

        Integer quarter;

        Integer week;

        LocalDate startDate;

        LocalDate endDate;

        public static WeekDto of(Week week) {
            Quarter quarter = week.getQuarter();
            return WeekDto.builder()
                    .year(quarter.getYearValue())
                    .quarter(quarter.getQuarterValue())
                    .week(week.getWeekValue())
                    .startDate(week.getStartDateTime().toLocalDate())
                    .endDate(week.getEndDateTime().toLocalDate())
                    .build();
        }

        public static WeekDto of(YQWRecord record) {
            return WeekDto.builder()
                    .year(record.yearValue())
                    .quarter(record.quarterValue())
                    .week(record.weekValue())
                    .build();
        }
    }
}
