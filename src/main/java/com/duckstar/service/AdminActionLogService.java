package com.duckstar.service;

import com.duckstar.domain.Anime;
import com.duckstar.domain.Member;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.enums.ManageFilterType;
import com.duckstar.domain.mapping.AdminActionLog;
import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.repository.AdminActionLog.AdminActionLogRepository;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.duckstar.web.dto.admin.AdminLogDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminActionLogService {

    private final AdminActionLogRepository adminActionLogRepository;

    public ManagementLogSliceDto getManagementLogs(Pageable pageable, ManageFilterType filterType) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        List<ManagementLogDto> rows = adminActionLogRepository
                .getManagementLogDtos(filterType, page * size, size + 1);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(hasNext)
                .page(page)
                .size(size)
                .build();

        return ManagementLogSliceDto.builder()
                .managementLogDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }

    @Transactional
    public AdminActionLog saveAdminActionLog(Member member, Anime anime, AdminTaskType type) {
        return adminActionLogRepository.save(
                AdminActionLog.builder()
                        .member(member)
                        .anime(anime)
                        .adminTaskType(type)
                        .build()
        );
    }

    @Transactional
    public AdminActionLog saveAdminActionLog(Member member, Episode episode, AdminTaskType type) {
        return adminActionLogRepository.save(
                AdminActionLog.builder()
                        .member(member)
                        .episode(episode)
                        .adminTaskType(type)
                        .build()
        );
    }
}
