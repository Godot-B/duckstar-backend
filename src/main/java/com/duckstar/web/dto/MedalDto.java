package com.duckstar.web.dto;

import com.duckstar.domain.enums.MedalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class MedalDto {

    @Builder
    @Getter
    public static class RackUnitDto {
        LocalDate startDate;

        LocalDate endDate;

        MedalPreviewDto medalPreviewDto;

//        VoteRatioDto voteRatioDto;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class MedalPreviewDto {
        MedalType type;

        Integer rank;

        Integer year;

        Integer quarter;

        Integer week;
    }
}
