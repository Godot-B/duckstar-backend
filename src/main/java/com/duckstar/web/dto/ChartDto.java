package com.duckstar.web.dto;

import com.duckstar.crawler.dto.CrawlerDto.CrawlerRankDto;
import com.duckstar.web.dto.AnimeResponseDto.AnimeRankDto;
import com.duckstar.web.dto.CharacterResponseDto.CharacterRankDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ChartDto {

    @Builder
    @Getter
    public static class AnimeRankSliceDto {
        List<AnimeRankDto> animeRanks;

        List<CrawlerRankDto> crawlerRanks;

        Boolean hasNext;
    }

    @Builder
    @Getter
    public static class CharacterRankSliceDto {
        List<CharacterRankDto> characterRanks;

        List<CrawlerRankDto> crawlerRanks;

        Boolean hasNext;
    }
}
