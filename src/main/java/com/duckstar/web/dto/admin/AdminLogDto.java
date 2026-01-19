package com.duckstar.web.dto.admin;

import com.duckstar.domain.Member;
import com.duckstar.domain.enums.AdminTaskType;
import com.duckstar.domain.mapping.AdminActionLog;
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

        Long weekId;
        Integer year;
        Integer quarter;
        Integer week;

        String ipHash;

        String reason;

        Boolean isUndoable;  // undo 가능한지 여부

        ManagerProfileDto memberProfileDto;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class ManagerProfileDto {
        Long memberId;

        String profileImageUrl;

        String managerNickname;

        AdminTaskType taskType;

        LocalDateTime managedAt;

        public static ManagerProfileDto of(Member member, AdminActionLog adminActionLog) {
            if (member == null || adminActionLog == null) {
                return null;
            }

            return ManagerProfileDto.builder()
                    .memberId(member.getId())
                    .profileImageUrl(member.getProfileImageUrl())
                    .managerNickname(member.getNickname())
                    .taskType(adminActionLog.getAdminTaskType())
                    .managedAt(adminActionLog.getCreatedAt())
                    .build();
        }
    }
}
