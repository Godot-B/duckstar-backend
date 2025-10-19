package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.EpisodeStar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EpisodeStarRepository extends JpaRepository<EpisodeStar, Long>, EpisodeStarRepositoryCustom {
    Optional<EpisodeStar> findByEpisode_IdAndWeekVoteSubmission_Id(Long episodeId, Long weekVoteSubmissionId);

    @Query("""
        SELECT es
        FROM EpisodeStar es
        JOIN es.weekVoteSubmission s
        JOIN s.week w
        WHERE w.id = :weekId
    """)
    List<EpisodeStar> findAllByWeekId(@Param("weekId") Long weekId);

    List<EpisodeStar> findAllByWeekVoteSubmission_Id(Long weekVoteSubmissionId);

    @Query(value = """
        SELECT es.*
        FROM episode_star es
        JOIN week_vote_submission s ON s.id = es.submission_id
        JOIN (
            SELECT s2.principal_key
            FROM week_vote_submission s2
                     JOIN episode_star es2 ON es2.submission_id = s2.id
            WHERE s2.week_id = :weekId
              AND es2.star_score > 0
            GROUP BY s2.principal_key
            HAVING COUNT(DISTINCT DATE(es2.created_at)) >= 2
        ) eligible ON eligible.principal_key = s.principal_key
        WHERE s.week_id = :weekId
    """, nativeQuery = true)
    List<EpisodeStar> findEligibleByWeekId(Long weekId);
}
