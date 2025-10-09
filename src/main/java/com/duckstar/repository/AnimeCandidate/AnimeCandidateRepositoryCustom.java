package com.duckstar.repository.AnimeCandidate;

import com.duckstar.web.dto.MedalDto.RackUnitDto;
import com.duckstar.web.dto.VoteResponseDto.AnimeCandidateDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.duckstar.web.dto.AnimeResponseDto.*;

public interface AnimeCandidateRepositoryCustom {
    List<Long> findValidIdsForWeek(Long ballotWeekId, List<Long> candidateIds);

    List<AnimeCandidateDto> getAnimeCandidateDtosByWeekId(Long weekId);

    List<AnimeRank_legacyDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable);

    List<RackUnitDto> getRackUnitDtosByAnimeId(Long animeId);
}
