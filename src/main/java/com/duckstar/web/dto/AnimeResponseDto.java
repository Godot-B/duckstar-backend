package com.duckstar.web.dto;

import com.duckstar.domain.enums.*;
import com.duckstar.validation.annotation.MedalTypeSubset;
import com.duckstar.web.dto.CharacterResponseDto.CharacterHomePreviewPage;
import com.duckstar.web.dto.MedalDto.MedalPreviewDto;
import com.duckstar.web.dto.MedalDto.RackPage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class AnimeResponseDto {

    AnimeInfoDto info;

    AnimeStatDto stat;

    // 초기 데이터: 첫 페이지
    RackPage rackPage;

    // 초기 데이터: 첫 페이지
    CharacterHomePreviewPage characterHomePreviewPage;

    @Builder
    @Getter
    public static class AnimeRankDto {
        CardDto card;

        @MedalTypeSubset(anyOf = {
                MedalType.GOLD,
                MedalType.SILVER,
                MedalType.BRONZE
        })
        List<MedalPreviewDto> medalPreviews;

        AnimeStatDto stat;

        VoteResponseDto.VoteRatioDto voteRatio;

        Long animeId;
    }

    @Builder
    @Getter
    public static class AnimeStatDto {
        Integer debutRank;

        LocalDate debutDate;

        Integer peakRank;

        LocalDate peakDate;

        Integer weeksOnTop10;
    }

    @Builder
    @Getter
    public static class AnimeRankPreviewDto {
        CardDto card;

        Double votePercent;
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
