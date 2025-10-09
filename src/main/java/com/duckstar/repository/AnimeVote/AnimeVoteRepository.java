package com.duckstar.repository.AnimeVote;

import com.duckstar.domain.mapping.legacy_vote.AnimeVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnimeVoteRepository extends JpaRepository<AnimeVote, Long>, AnimeVoteRepositoryCustom {
    int countAllByAnimeCandidate_Anime_IdAndWeekVoteSubmission_Member_Id(Long animeId, Long memberId);

    @Query("""
        SELECT av
        FROM AnimeVote av
        JOIN av.weekVoteSubmission s
        JOIN s.week w
        WHERE w.id = :weekId
    """)
    List<AnimeVote> findAllByWeekId(@Param("weekId") Long weekId);

    List<AnimeVote> findAllByWeekVoteSubmission_IdAndAnimeCandidate_IdIn(Long submissionId, List<Long> candidateIds);

    void deleteAllByWeekVoteSubmission_IdAndAnimeCandidate_IdIn(Long submissionId, List<Long> candidateIds);

    List<AnimeVote> findAllByAnimeCandidate_Id(Long animeCandidateId);

    void deleteAllByAnimeCandidate_Id(Long animeCandidateId);

    void deleteAllByWeekVoteSubmission_Id(Long weekVoteSubmissionId);
}
