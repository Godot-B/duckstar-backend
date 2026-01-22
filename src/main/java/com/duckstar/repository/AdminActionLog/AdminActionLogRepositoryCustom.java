package com.duckstar.repository.AdminActionLog;

import com.duckstar.domain.enums.ManageFilterType;

import java.util.List;

import static com.duckstar.web.dto.admin.AdminLogDto.*;

public interface AdminActionLogRepositoryCustom {
    List<ManagementLogDto> getManagementLogDtos(ManageFilterType filterType, int offset, int limit);
}
