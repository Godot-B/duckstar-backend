package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.EpisodeStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeStarRepository extends JpaRepository<EpisodeStar, Long>, EpisodeStarRepositoryCustom {
    Optional<EpisodeStar> findByEpisode_IdAndWeekVoteSubmission_Id(Long episodeId, Long weekVoteSubmissionId);

    List<EpisodeStar> findAllByWeekVoteSubmission_Id(Long weekVoteSubmissionId);
}
