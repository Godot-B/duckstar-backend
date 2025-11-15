package com.duckstar.repository.AdminActionLog;

import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.duckstar.web.dto.admin.AdminLogDto.*;

public interface AdminActionLogRepositoryCustom {
    List<IpManagementLogDto> getIpManagementLogDtos(Pageable pageable);
}
