package com.duckstar.web.dto;

import com.duckstar.domain.enums.MedalType;
import com.duckstar.web.dto.VoteResponseDto.VoteRatioDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class MedalDto {

    @Builder
    @Getter
    public static class RackUnitDto {
        LocalDate startDate;

        LocalDate endDate;

        MedalPreviewDto medalPreviewDto;

        VoteRatioDto voteRatioDto;
    }

    @Builder
    @Getter
    public static class MedalPreviewDto {
        MedalType type;

        Integer rank;

        Integer year;

        Integer quarter;

        Integer week;
    }
}
