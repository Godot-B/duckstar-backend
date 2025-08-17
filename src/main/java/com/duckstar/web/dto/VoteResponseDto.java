package com.duckstar.web.dto;

import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.VoteCategory;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.Builder;
import lombok.Getter;

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
    }

    @Builder
    @Getter
    public static class AnimeCandidateDto {
        Long animeCandidateId;

        String mainThumbnailUrl;

        String titleKor;
    }
}
