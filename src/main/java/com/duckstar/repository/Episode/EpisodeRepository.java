package com.duckstar.repository.Episode;

import com.duckstar.domain.Anime;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.legacy_vote.AnimeCandidate;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long>, EpisodeRepositoryCustom {
    Optional<Episode> findEpisodeByAnimeAndScheduledAtLessThanEqualAndNextEpScheduledAtGreaterThan(Anime anime, LocalDateTime time1, LocalDateTime time2);
    Optional<Episode> findTopByAnimeOrderByEpisodeNumberDesc(Anime anime);

    List<Episode> findAllByAnime_IdOrderByScheduledAtAsc(Long animeId);
    List<Episode> findAllByScheduledAtGreaterThanEqualAndScheduledAtLessThan(LocalDateTime scheduledAtIsGreaterThan, LocalDateTime scheduledAtIsLessThan);

    @Query("""
             SELECT e
             FROM Episode e
             JOIN e.anime a
             WHERE e.week.id = :weekId
             ORDER BY
                e.rankInfo.rank ASC NULLS LAST,
                e.rankInfo.rankedVoterCount DESC,
                e.rankInfo.rankedAverage DESC,
                a.titleKor ASC
            """)
    List<Episode> findEpisodesByWeekOrdered(
            @Param("weekId") Long weekId,
            Pageable pageable
    );

    List<Episode> findAllByAnime_IdOrderByScheduledAtDesc(Long animeId);
}
