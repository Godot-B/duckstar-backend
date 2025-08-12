package com.duckstar.web.dto;

import com.duckstar.web.dto.AnimeResponseDto.AnimeRankPreviewDto;
import com.duckstar.web.dto.CharacterResponseDto.CharacterRankPreviewDto;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class HomeDto {

    List<HotAnimeDto> hotAnimes;

    List<HotCharacterDto> hotCharacters;

    WeeklyTopDto weeklyTop;

    @Size(max = 12, message = "드롭다운용 weeks 리스트는 최대 12개까지입니다.")
    List<WeekDto> weeks;

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
        // 애니메이션 톱 N
        List<AnimeRankPreviewDto> animeRankPreviews;

        List<CardDto> animeTrendAnimeRanks;

        List<CardDto> aniLabAnimeRanks;

        // 남캐 톱 N
        List<CharacterRankPreviewDto> heroRankPreviews;

        List<CardDto> animeTrendHeroRanks;

        // 여캐 톱 N
        List<CharacterRankPreviewDto> heroineRankPreviews;

        List<CardDto> animeTrendHeroineRanks;
    }
}
