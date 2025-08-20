package com.duckstar.repository.AnimeVote;

import com.duckstar.domain.mapping.AnimeVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeVoteRepository extends JpaRepository<AnimeVote, Long>, AnimeVoteRepositoryCustom {
    int countAllByAnimeCandidate_Anime_IdAndWeekVoteSubmission_Member_Id(Long animeId, Long memberId);
}
