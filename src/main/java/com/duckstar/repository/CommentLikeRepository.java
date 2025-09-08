package com.duckstar.repository;

import com.duckstar.domain.Member;
import com.duckstar.domain.mapping.CommentLike;
import com.duckstar.domain.mapping.comment.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentAndMember(Comment comment, Member member);
}
