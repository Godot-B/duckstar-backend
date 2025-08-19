package com.duckstar.repository.AnimeSeason;

import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;

import java.time.LocalDateTime;
import java.util.List;

public interface AnimeSeasonRepositoryCustom {
    List<AnimePreviewDto> getSeasonAnimePreviewsByQuarterAndWeek(
            Long quarterId,
            LocalDateTime weekStart,
            LocalDateTime weekEnd
    );
    List<SeasonDto> getSeasonDtosByAnimeId(Long animeId);
}
