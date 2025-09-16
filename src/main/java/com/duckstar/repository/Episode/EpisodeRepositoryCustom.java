package com.duckstar.repository.Episode;

import com.duckstar.web.dto.EpisodeDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EpisodeRepositoryCustom {
    List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId);
}
