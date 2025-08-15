package com.duckstar.web.dto;

import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.CharacterResponseDto.CharacterRankDto;
import com.duckstar.web.dto.RankInfoDto.RankPreviewDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ChartDto {

    @Builder
    @Getter
    public static class AnimeRankSliceDto {
        List<AnimeRankDto> animeRankDtos;

        List<RankPreviewDto> animeTrendRankDtos;

        List<RankPreviewDto> aniLabRankDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    public static class CharacterRankSliceDto {
        List<CharacterRankDto> characterRankDtos;

        List<RankPreviewDto> crawlerRankDtos;

        PageInfo pageInfo;
    }
}
