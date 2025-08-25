package com.duckstar.web.dto;

import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.enums.SeasonType;
import com.duckstar.domain.enums.VoteCategory;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class VoteResponseDto {

    @Builder
    @Getter
    public static class VoteReceiptDto {
        Long submissionId;

        WeekDto weekDto;

        VoteCategory category;

        Integer normalCount;

        Integer bonusCount;

        LocalDateTime submittedAt;
    }

    @Builder
    @Getter
    public static class VoteCheckDto {
        Boolean hasVoted;

        Long submissionId;

        public static VoteCheckDto of(Long submissionId) {
            boolean hasVoted = submissionId != null;
            return VoteCheckDto.builder()
                    .hasVoted(hasVoted)
                    .submissionId(submissionId)
                    .build();
        }
    }

    @Builder
    @Getter
    public static class AnimeVoteHistoryDto {
        Long submissionId;

        WeekDto weekDto;

        VoteCategory category;

        LocalDateTime submittedAt;

        List<AnimeBallotDto> animeBallotDtos;
    }

    @Builder
    @Getter
    public static class AnimeBallotDto {
        BallotType ballotType;

        Long animeId;

        String mainThumbnailUrl;
    }

    @Builder
    @Getter
    public static class AnimeCandidateListDto {
        Long weekId;

        WeekDto weekDto;

        List<AnimeCandidateDto> animeCandidates;
        Integer candidatesCount;
    }

    @Getter
    @AllArgsConstructor
    public static class AnimeCandidateDto {
        Long animeCandidateId;

        String mainThumbnailUrl;

        String titleKor;

        Medium medium;
    }
}
