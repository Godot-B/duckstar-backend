package com.duckstar.repository.Episode;

import com.duckstar.web.dto.WeekResponseDto.EpisodeDto;

import java.util.List;

public interface EpisodeRepositoryCustom {

    List<EpisodeDto> getEpisodesByAnimeId(Long animeId);
}
