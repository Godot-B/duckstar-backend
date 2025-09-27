package com.duckstar.repository.Episode;

import com.duckstar.domain.Anime;
import com.duckstar.domain.mapping.Episode;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, Long>, EpisodeRepositoryCustom {
    Optional<Episode> findEpisodeByAnimeAndScheduledAtLessThanEqualAndNextEpScheduledAtGreaterThan(Anime anime, LocalDateTime time1, LocalDateTime time2);
    Optional<Episode> findTopByAnimeOrderByEpisodeNumberDesc(Anime anime);

    List<Episode> findAllByAnime_IdOrderByEpisodeNumberAsc(Long animeId);
}
