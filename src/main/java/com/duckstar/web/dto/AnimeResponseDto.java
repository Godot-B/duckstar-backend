package com.duckstar.web.dto;

import com.duckstar.domain.enums.*;
import com.duckstar.validation.annotation.MedalTypeSubset;
import com.duckstar.web.dto.MedalDto.MedalPreviewDto;
import com.duckstar.web.dto.MedalDto.RackUnitDto;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import com.duckstar.web.dto.RankInfoDto.VoteRatioDto;
import com.duckstar.web.dto.WeekResponseDto.EpisodeDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AnimeResponseDto {

    @Builder
    @Getter
    public static class AnimeHomeDto {
        AnimeInfoDto animeInfoDto;

        AnimeStatDto animeStatDto;

        List<EpisodeDto> episodeDtos;

        List<RackUnitDto> rackUnitDtos;

        List<CastPreviewDto> castPreviews;
    }
    
    @Builder
    @Getter
    public static class AnimeRankDto {
        Long animeId;

        RankPreviewDto rankPreviewDto;

        @MedalTypeSubset(anyOf = {
                MedalType.GOLD,
                MedalType.SILVER,
                MedalType.BRONZE
        })
        List<MedalPreviewDto> medalPreviews;

        AnimeStatDto animeStatDto;

        VoteRatioDto voteRatioDto;
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
    public static class AnimeInfoDto {
        Medium medium;

        AnimeStatus status;

        Integer totalEpisodes;

        LocalDateTime premiereDateTime;

        String titleKor;

        String titleOrigin;

        DayOfWeekShort dayOfWeek;

        String airTime;

        String corp;

        String director;

        String genre;

        String author;

        Integer minAge;

        Map<SiteType, String> officalSite;

        String mainImageUrl;

        List<SeasonDto> seasonDtos;

        List<OttDto> ottDtos;
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
    
    @Builder
    @Getter
    public static class CastPreviewDto {
        String mainThumbnailUrl;

        String nameKor;

        String cv;
    }
}
