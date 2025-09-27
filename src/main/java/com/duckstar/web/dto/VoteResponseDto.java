package com.duckstar.web.dto;

import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AnimeVote;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class VoteResponseDto {

    @Builder
    @Getter
    public static class AnimeVoteHistoryDto {
        Boolean hasVoted;

        String nickName;

        Long submissionId;

        WeekDto weekDto;

        VoteCategory category;

        Integer normalCount;

        Integer bonusCount;

        LocalDateTime submittedAt;

        List<AnimeBallotDto> animeBallotDtos;

        public static AnimeVoteHistoryDto ofEmpty() {
            return AnimeVoteHistoryDto.builder()
                    .hasVoted(false)
                    .build();
        }
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AnimeBallotDto {
        BallotType ballotType;

        Long animeId;

        String mainThumbnailUrl;

        String titleKor;
    }

    @Builder
    @Getter
    public static class AnimeCandidateListDto {
        VoteStatus status;

        Long weekId;

        WeekDto weekDto;

        List<AnimeCandidateDto> animeCandidates;
        Integer candidatesCount;

        public static AnimeCandidateListDto ofEmpty(VoteStatus status) {
            return AnimeCandidateListDto.builder()
                    .status(status)
                    .build();
        }
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
