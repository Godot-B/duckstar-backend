package com.duckstar.repository;

import com.duckstar.domain.mapping.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
