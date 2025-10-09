package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.Episode;

import java.util.Map;

public interface EpisodeStarRepositoryCustom {
    Map<Episode, Integer> findEpisodeMapBySubmissionId(Long submissionId);
}
