package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;

import java.util.List;

public interface EpisodeStarRepositoryCustom {
    List<EpisodeStar> findAllEligibleByWeekId(Long weekId);

    Long getVoteTimeLeftForLatestEpVoted(Long submissionId);
}
