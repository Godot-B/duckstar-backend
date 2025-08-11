package com.duckstar.web.dto;

import com.duckstar.crawler.dto.CrawlerDto;
import com.duckstar.crawler.dto.CrawlerDto.CrawlerRankDto;
import com.duckstar.web.dto.AnimeResponseDto.AnimeRankPreviewDto;
import com.duckstar.web.dto.CharacterResponseDto.CharacterRankPreviewDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class HomeDto {

    List<HotAnimeDto> hotAnimes;

    List<HotCharacterDto> hotCharacters;

    WeeklyTopDto weeklyTop;

    List<WeekDto> weeksMenu;

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

    @Builder
    @Getter
    public static class WeeklyTopDto {
        List<AnimeRankPreviewDto> animeRankPreviews;

        List<CrawlerRankDto> animeTrendAnimeRanks;

        List<CrawlerRankDto> aniLabAnimeRanks;

        List<CharacterRankPreviewDto> heroRankPreviews;

        List<CrawlerRankDto> animeTrendHeroRanks;

        List<CharacterRankPreviewDto> heroineRankPreviews;

        List<CrawlerRankDto> animeTrendHeroineRanks;
    }
}
