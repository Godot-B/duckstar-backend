package com.duckstar.repository.AdminActionLog;

import com.duckstar.domain.mapping.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long>, AdminActionLogRepositoryCustom {
    AdminActionLog findTopByOrderByCreatedAtDesc();

    List<AdminActionLog> findAllByAnime_Id(Long animeId);
}
