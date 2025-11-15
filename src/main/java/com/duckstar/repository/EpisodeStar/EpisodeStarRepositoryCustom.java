package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.EpisodeStar;

import java.util.List;
import java.util.Map;

public interface EpisodeStarRepositoryCustom {
    Map<Episode, Integer> findEpisodeMapBySubmissionId(Long submissionId);

    List<EpisodeStar> findAllEligibleByWeekId(Long weekId);

    Long getVoteTimeLeftForLatestEpVoted(Long submissionId);
}
