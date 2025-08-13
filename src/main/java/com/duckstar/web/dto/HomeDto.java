package com.duckstar.web.dto;

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

    HotBannerDto hotBannerDto;

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
    public static class HotBannerDto {
        List<HotAnimeDto> hotAnimeDtos;

        List<HotCharacterDto> hotCharacterDtos;
    }

    @Builder
    @Getter
    public static class HotAnimeDto {
        String titleKor;

        String imageUrl;
    }

    @Builder
    @Getter
    public static class HotCharacterDto {
        String nameKor;

        String animeTitleKor;

        String imageUrl;

        String animeImgUrl;
    }
}
