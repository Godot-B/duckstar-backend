package com.duckstar.repository;

import com.duckstar.domain.mapping.EpisodeStar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodeStarRepository extends JpaRepository<EpisodeStar, Long> {
    Optional<EpisodeStar> findByEpisode_IdAndWeekVoteSubmission_Id(Long episodeId, Long weekVoteSubmissionId);
}
