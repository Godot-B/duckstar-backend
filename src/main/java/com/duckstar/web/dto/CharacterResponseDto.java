package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class CharacterResponseDto {

    @Builder
    @Getter
    public static class CharacterRankPreviewDto {
        Integer rank;

        Integer rankDiff;

        Integer consecutiveWeeksAtSameRank;

        String thumbnailUrl;

        String nameKor;

        String animeTitleKor;

        Double votePercent;
    }

    @Builder
    @Getter
    public static class CharacterRankDto {
        CharacterRankPreviewDto rankPreview;

        List<MedalDto> medals;

        Long characterId;

        // 데뷔 순위 없다

        Integer peakRank;

        LocalDate peakDate;

        Integer weeksOnTop10;

        Integer malePercent;

        Integer femalePercent;
    }
}
