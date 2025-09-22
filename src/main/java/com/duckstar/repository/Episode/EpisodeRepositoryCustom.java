package com.duckstar.repository.Episode;

import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;

public interface EpisodeRepositoryCustom {
    List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId);
}
