package com.duckstar.web.dto;

import com.duckstar.domain.HomeBanner;
import com.duckstar.domain.enums.BannerType;
import com.duckstar.domain.enums.ContentType;
import com.duckstar.web.dto.SummaryDto.RankPreviewDto;
import com.duckstar.web.dto.SummaryDto.RankSummaryDto;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

import static com.duckstar.web.dto.WeekResponseDto.*;

@Builder
@Getter
public class HomeDto {

    WeeklyTopDto weeklyTopDto;

    List<HomeBannerDto> homeBannerDtos;

    @Size(max = 12, message = "드롭다운용 weeks 리스트는 최대 12개까지입니다.")
    List<WeekDto> weekDtos;

    @Builder
    @Getter
    public static class WeeklyTopDto {
        List<RankPreviewDto> rankPreviews;

        List<RankSummaryDto> crawlerRankDtos;
    }

    @Builder
    @Getter
    public static class HomeBannerDto {
        BannerType bannerType;

        ContentType contentType;

        String mainTitle;

        String subTitle;

        String animeImageUrl;

        String characterImageUrl;

        public static HomeBannerDto from(HomeBanner banner) {
            return HomeBannerDto.builder()
                    .bannerType(banner.getBannerType())
                    .contentType(banner.getContentType())
                    .mainTitle(banner.getMainTitle())
                    .subTitle(banner.getSubTitle())
                    .animeImageUrl(banner.getAnimeImageUrl())
                    .characterImageUrl(banner.getCharacterImageUrl())
                    .build();
        }
    }
}
