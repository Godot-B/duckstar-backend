package com.duckstar.web.dto;

import com.duckstar.domain.enums.MedalType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class MedalDto {

    @Builder
    @Getter
    public static class RackPage {
        List<RackUnitDto> rackUnits;

        // 페이징 관련
        Integer listSize;
        Integer totalPage;
        Long totalElements;
        Boolean isFirst;
        Boolean isLast;
    }

    @Builder
    @Getter
    public static class RackUnitDto {
        MedalPreviewDto medalPreview;

        LocalDate startDate;

        LocalDate endDate;

        VoteResponseDto.VoteRatioDto voteRatio;
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
