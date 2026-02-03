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

import static com.duckstar.web.dto.WeekResponseDto.*;

public class AdminLogDto {

    @Builder
    @Getter
    public static class ManagementLogSliceDto {
        List<ManagementLogDto> managementLogDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class ManagementLogDto {
        Long logId;

        // 관계되는 하나만 셋팅됨
        Long animeId;
        Long episodeId;
        String ipHash;
        // 에피소드 관리
            // 애니메이션 관리
            String titleKor;
        Integer episodeNumber;

        // IP 관리
        Long weekId;
        WeekDto weekDto;
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
