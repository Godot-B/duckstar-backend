package com.duckstar.repository.EpisodeStar;

import com.duckstar.domain.mapping.weeklyVote.EpisodeStar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpisodeStarRepository extends JpaRepository<EpisodeStar, Long>, EpisodeStarRepositoryCustom {
    int countAllByEpisode_Anime_IdAndWeekVoteSubmission_Member_Id(Long animeId, Long memberId);

    Optional<EpisodeStar> findByEpisode_IdAndWeekVoteSubmission_Id(Long episodeId, Long weekVoteSubmissionId);

    List<EpisodeStar> findAllByWeekVoteSubmission_Id(Long weekVoteSubmissionId);
}
