package com.duckstar.repository.Episode;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;

public interface EpisodeRepositoryCustom {
    List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId);
    List<AnimePreviewDto> getAnimePreviewsByWeek(LocalDateTime weekStart, LocalDateTime weekEnd);
}
