package com.duckstar.repository.AnimeCandidate;

import com.duckstar.domain.vo.RankInfo;
import com.duckstar.web.dto.MedalDto.RackUnitDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

import static com.duckstar.web.dto.AnimeResponseDto.*;

public interface AnimeCandidateRepositoryCustom {
    List<Long> findValidIdsForWeek(Long ballotWeekId, List<Long> candidateIds);

    List<AnimeRank_legacyDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable);

    List<RackUnitDto> getRackUnitDtosByAnimeId(Long animeId);
}
