package com.duckstar.repository;

import com.duckstar.domain.Member;
import com.duckstar.domain.mapping.Reply;
import com.duckstar.domain.mapping.ReplyLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {
    Optional<ReplyLike> findByReplyAndMember(Reply reply, Member member);
}
