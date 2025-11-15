package com.duckstar.service;

import com.duckstar.repository.AdminActionLog.AdminActionLogRepository;
import com.duckstar.web.dto.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

    public IpManagementLogSliceDto getIpManagementLogs(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        Pageable overFetch = PageRequest.of(
                page,
                size + 1,
                pageable.getSort()
        );

        List<IpManagementLogDto> rows =
                adminActionLogRepository.getIpManagementLogDtos(overFetch);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        PageInfo pageInfo = PageInfo.builder()
                .hasNext(hasNext)
                .page(page)
                .size(size)
                .build();

        return IpManagementLogSliceDto.builder()
                .ipManagementLogDtos(rows)
                .pageInfo(pageInfo)
                .build();
    }
}
