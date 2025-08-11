package com.duckstar.web.dto;

import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.enums.OttType;
import com.duckstar.domain.enums.SeasonType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @Builder
    @Getter
    public static class AnimeInfoDto {
        String imageUrl;

        String titleKor;

        String titleOrigin;

        List<SeasonDto> seasons;

        Medium medium;  // TVA, MOVIE

        DayOfWeekShort dayOfWeek;

        String airTime;

        List<OttDto> otts;

        String corp;

        String director;

        String genre;

        String author;

        LocalDate premiereDate;

        String maturityRating;

        Map<String, String> officalSite;
    }

    @Builder
    @Getter
    public static class SeasonDto {
        Integer year;
        SeasonType seasonType;
    }

    @Builder
    @Getter
    public static class OttDto {
        OttType ottType;
        String watchUrl;
    }
}
