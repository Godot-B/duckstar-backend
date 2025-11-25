package com.duckstar.repository.Episode;

import com.duckstar.domain.Week;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.service.AnimeService.*;
import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

public interface EpisodeRepositoryCustom {
    List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId);

    List<LiveCandidateDto> getLiveCandidateDtos(List<String> principalKeys);

    List<WeekCandidateDto> getWeekCandidateDtos(Long weekId, String principalKey);

    Boolean isHybridTime(Week currentWeek, LocalDateTime now);

    List<AnimePreviewDto> getAnimePreviewsByDuration(LocalDateTime weekStart, LocalDateTime weekEnd);

    List<AnimeRankDto> getAnimeRankDtosByWeekIdWithOverFetch(Long weekId, LocalDateTime weekEndDateTime, Pageable pageable);

    List<PremieredEpRecord> findPremieredEpRecordsInWindow(LocalDateTime windowStart, LocalDateTime windowEnd);

    Optional<CandidateFormDto> getCandidateFormDto(Long episodeId, List<String> principalKeys);
}
