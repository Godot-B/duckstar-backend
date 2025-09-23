package com.duckstar.repository.AnimeSeason;

import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;
import com.duckstar.web.dto.SearchResponseDto.AnimePreviewDto;

import java.util.List;

public interface AnimeSeasonRepositoryCustom {
    List<AnimePreviewDto> getAnimePreviewsByQuarter(Long quarterId);
    List<SeasonDto> getSeasonDtosByAnimeId(Long animeId);
}
