package com.duckstar.web.dto.admin;

import com.duckstar.web.dto.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class SubmissionResponseDto {

    @Builder
    @Getter
    public static class SubmissionCountSliceDto {
        List<SubmissionCountDto> submissionCountDtos;

        PageInfo pageInfo;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class SubmissionCountDto {
        Long weekId;
        Integer year;
        Integer quarter;
        Integer week;

        String ipHash;
        Long count;
        Boolean isBlocked;  // 전역 차단 여부
        Boolean isAllWithdrawn;  // 몰수 여부

        LocalDateTime firstCreatedAt;  // 첫 제출 시각
        LocalDateTime lastCreatedAt;  // 마지막 제출 시각
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class EpisodeStarDto {
        String titleKor;
        Integer starScore;
        Boolean isBlocked;

        LocalDateTime createdAt;
        LocalDateTime updatedAt;
    }
}
