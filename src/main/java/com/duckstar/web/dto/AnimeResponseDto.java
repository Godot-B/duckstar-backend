package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public class AnimeResponseDto {

    @Builder
    @Getter
    public static class AnimeRankPreviewDto {
        Integer rank;

        Integer rankDiff;

        Integer consecutiveWeeksAtSameRank;

        String thumbnailUrl;

        String nameKor;

        String corp;

        Double votePercent;
    }

    @Builder
    @Getter
    public static class AnimeRankDto {
        AnimeRankPreviewDto rankPreview;

        List<MedalDto> medals;

        Long animeId;

        Integer debutRank;

        LocalDate debutDate;

        Integer peakRank;

        LocalDate peakDate;

        Integer weeksOnTop10;

        Double malePercent;

        Double femalePercent;
    }
}
