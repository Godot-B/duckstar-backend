package com.duckstar.repository.AnimeSeason;

import com.duckstar.web.dto.AnimeResponseDto.SeasonDto;

import java.util.List;

public interface AnimeSeasonRepositoryCustom {
    List<SeasonDto> getSeasonDtosByAnimeId(Long animeId);
}
