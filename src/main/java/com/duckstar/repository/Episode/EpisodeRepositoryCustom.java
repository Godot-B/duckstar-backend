package com.duckstar.repository.Episode;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

public interface EpisodeRepositoryCustom {
    List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId);
    List<StarCandidateDto> getStarCandidatesByDuration(LocalDateTime weekStart, LocalDateTime weekEnd);
    List<AnimePreviewDto> getAnimePreviewsByDuration(LocalDateTime weekStart, LocalDateTime weekEnd);

    List<AnimeRankDto> getAnimeRankDtosByWeekIdWithOverFetch(Long weekId, Pageable pageable);
}
