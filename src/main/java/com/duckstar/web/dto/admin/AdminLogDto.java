package com.duckstar.web.dto.admin;

import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.web.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class AdminLogDto {

    @Builder
    @Getter
    public static class IpManagementLogSliceDto {
        List<IpManagementLogDto> ipManagementLogDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class IpManagementLogDto {
        Long logId;

        Long memberId;

        String profileImageUrl;

        String managerNickname;

        Long weekId;
        Integer year;
        Integer quarter;
        Integer week;

        String ipHash;

        AdminTaskType adminTaskType;
        String reason;

        Boolean isUndoable;  // undo 가능한지 여부

        LocalDateTime managedAt;
    }
}
