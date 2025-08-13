package com.duckstar.web.dto;

import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.CharacterResponseDto.CharacterRankDto;
import com.duckstar.web.dto.SummaryDto.RankSummaryDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ChartDto {

    @Builder
    @Getter
    public static class AnimeRankSliceDto {
        List<AnimeRankDto> animeRankDtos;

        List<RankSummaryDto> crawlerRankDtos;

        Boolean hasNext;
    }

    @Builder
    @Getter
    public static class AniLabRankSliceDto {
        List<RankSummaryDto> crawlerRankDtos;

        Boolean hasNext;
    }

    @Builder
    @Getter
    public static class CharacterRankSliceDto {
        List<CharacterRankDto> characterRankDtos;

        List<RankSummaryDto> crawlerRankDtos;

        Boolean hasNext;
    }
}
