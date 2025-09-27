package com.duckstar.repository.Reply;

import com.duckstar.domain.mapping.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long>, ReplyRepositoryCustom {
    Integer countAllByParent_id(Long parentId);

    List<Reply> findAllByAuthor_Id(Long authorId);
}
