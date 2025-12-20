package com.duckstar.web.dto;

import com.duckstar.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SurveyResponseDto {

    @Builder
    @Getter
    public static class SurveyDto {
        SurveyStatus status;

        Integer year;
        SurveyType type;
        LocalDate startDate;
        LocalDate endDate;
    }

    @Builder
    @Getter
    public static class AnimeVoteHistoryDto {
        Boolean hasVoted;

        Long memberId;
        String nickName;

        Long submissionId;

        Integer year;
        SurveyType type;

        Integer normalCount;
        Integer bonusCount;

        LocalDateTime submittedAt;
        List<AnimeBallotDto> animeBallotDtos;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AnimeBallotDto {
        BallotType ballotType;

        Long animeCandidateId;

        Long animeId;

        String mainThumbnailUrl;

        String titleKor;

        // 투표 결과 화면 - 에피소드 별 댓글 남기기 기능을 위해
        Integer totalEpisodes;
    }

    @Builder
    @Getter
    public static class AnimeCandidateListDto {

        List<AnimeCandidateDto> animeCandidates;
        Integer candidatesCount;

        // 회원 배려 선입력 필드
        Gender memberGender;
        AgeGroup memberAgeGroup;
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
