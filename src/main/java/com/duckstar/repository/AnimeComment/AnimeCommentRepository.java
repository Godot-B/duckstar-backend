package com.duckstar.repository.AnimeComment;

import com.duckstar.domain.mapping.comment.AnimeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnimeCommentRepository extends JpaRepository<AnimeComment, Long>, AnimeCommentRepositoryCustom {
}
