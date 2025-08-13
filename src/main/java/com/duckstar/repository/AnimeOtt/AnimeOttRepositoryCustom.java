package com.duckstar.repository.AnimeOtt;

import com.duckstar.web.dto.AnimeResponseDto.OttDto;

import java.util.List;

public interface AnimeOttRepositoryCustom {
    List<OttDto> getOttDtosByAnimeId(Long animeId);
}
