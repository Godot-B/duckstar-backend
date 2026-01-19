package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.mapping.comment.AnimeComment;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AnimeCommentRepository extends JpaRepository<AnimeComment, Long>, AnimeCommentRepositoryCustom {
    List<AnimeComment> findAllByAuthor_Id(Long authorId);

    Optional<AnimeComment> findByEpisodeStar_Id(Long episodeStarId);

    List<AnimeComment> findAllByEpisode_IdIn(Collection<Long> episodeIds);

    List<AnimeComment> findAllByAnime_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(Long animeId, LocalDateTime createdAtIsGreaterThan);

    boolean existsByEpisode(Episode episode);
}
