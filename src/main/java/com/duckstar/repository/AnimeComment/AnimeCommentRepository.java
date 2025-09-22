package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.mapping.comment.AnimeComment;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnimeCommentRepository extends JpaRepository<AnimeComment, Long>, AnimeCommentRepositoryCustom {
    List<AnimeComment> findAllByEpisode_Id(Long episodeId);
    List<AnimeComment> findAllByAnime_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(Long animeId, LocalDateTime createdAtIsGreaterThan, LocalDateTime createdAtIsLessThan);
}
