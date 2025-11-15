package com.duckstar.repository.AdminActionLog;

import com.duckstar.domain.mapping.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long>, AdminActionLogRepositoryCustom {
}
