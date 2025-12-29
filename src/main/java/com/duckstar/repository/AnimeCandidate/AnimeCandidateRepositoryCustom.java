package com.duckstar.repository.AnimeCandidate;

import java.util.List;

public interface AnimeCandidateRepositoryCustom {
    List<Long> findValidIdsForWeek(Long ballotWeekId, List<Long> candidateIds);

//    List<AnimeRank_legacyDto> getAnimeRankDtosByWeekId(Long weekId, Pageable pageable);

//    List<RackUnitDto> getRackUnitDtosByAnimeId(Long animeId);
}
