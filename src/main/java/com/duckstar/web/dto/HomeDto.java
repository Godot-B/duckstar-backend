package com.duckstar.web.dto;

import com.duckstar.domain.Anime;
import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.enums.BannerType;
import com.duckstar.domain.enums.ContentType;
import com.duckstar.web.dto.RankInfoDto.DuckstarRankPreviewDto;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.duckstar.web.dto.WeekResponseDto.*;

@Builder
@Getter
public class HomeDto {

    WeeklyTopDto weeklyTopDto;

    List<HomeBannerDto> homeBannerDtos;

    WeekDto currentWeekDto;

    @Size(max = 12, message = "드롭다운용 weeks 리스트는 최대 12개까지입니다.")
    List<WeekDto> pastWeekDtos;

    @Builder
    @Getter
    public static class WeeklyTopDto {
        Boolean isPrepared;

        List<DuckstarRankPreviewDto> duckstarRankPreviews;

        List<RankPreviewDto> animeCornerRankPreviews;
        List<RankPreviewDto> anilabRankPreviews;
    }

    @Builder
    @Getter
    public static class HomeBannerDto {
        BannerType bannerType;

        ContentType contentType;

        Long animeId;

        Long characterId;

        String mainTitle;

        String subTitle;

        String animeImageUrl;

        String characterImageUrl;

        public static HomeBannerDto ofAnime(HomeBanner banner) {
            LocalDateTime endDateTime = banner.getWeek().getEndDateTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d");
            String formatted = null;
            if (endDateTime != null) {
                formatted = endDateTime.format(formatter);
            }

            Anime anime = banner.getAnime();
            String override = banner.getAnimeImageUrl();
            return HomeBannerDto.builder()
                    .bannerType(banner.getBannerType())
                    .contentType(banner.getContentType())
                    .animeId(anime.getId())
                    .characterId(null)
                    .mainTitle(anime.getTitleKor())
                    .subTitle("덕스타, " + formatted + " 기준")
                    .animeImageUrl(override != null ? override : anime.getMainImageUrl())
                    .characterImageUrl(null)
                    .build();
        }
    }
}
