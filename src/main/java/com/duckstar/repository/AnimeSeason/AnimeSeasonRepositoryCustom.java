package com.duckstar.repository.AnimeSeason;

import com.duckstar.domain.Anime;
import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;
import com.duckstar.web.dto.SearchResponseDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;

import java.util.List;

public interface AnimeSeasonRepositoryCustom {
    List<AnimePreviewDto> getSeasonAnimesByQuarterId(Long quarterId, Long currentWeekId);
    List<SeasonDto> getSeasonDtosByAnimeId(Long animeId);
}
