package com.duckstar.repository.Episode;

import com.duckstar.domain.mapping.weeklyVote.Episode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.duckstar.service.AnimeService.AnimeCommandServiceImpl.*;
import static com.duckstar.web.dto.AnimeResponseDto.*;
import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.SearchResponseDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;

public interface EpisodeRepositoryCustom {
    List<EpisodeDto> getEpisodeDtosByAnimeId(Long animeId);

    List<LiveCandidateDto> getLiveCandidateDtos(List<String> principalKeys);

    List<WeekCandidateDto> getWeekCandidateDtos(Long weekId, String principalKey);

    List<AnimePreviewDto> getAnimePreviewsByDuration(LocalDateTime weekStart, LocalDateTime weekEnd);

    List<AnimeRankDto> getAnimeRankDtosByWeekId(Long weekId, LocalDateTime weekEndDateTime, int offset, int limit);

    List<PremieredEpRecord> findPremieredEpRecordsInWindow(LocalDateTime windowStart, LocalDateTime windowEnd);

    Optional<CandidateFormDto> getCandidateFormDto(Long episodeId, List<String> principalKeys);

    List<AdminEpisodeDto> getEpisodeInfoDtosByAnimeId(Long animeId);

    List<ScheduleInfoDto> getScheduleInfoDtosByWeekId(Long weekId);

    List<Episode> findEpisodesByReleaseOrderByAnimeId(Long animeId);
}
