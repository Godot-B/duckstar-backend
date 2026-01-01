package com.duckstar.web.dto;

import com.duckstar.domain.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.VoteResponseDto.*;

public class SurveyResponseDto {

    @Builder
    @Getter
    public static class SurveyDto {
        String thumbnailUrl;

        Long surveyId;
        Boolean hasVoted;
        SurveyStatus status;

        Integer year;
        SurveyType type;
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
    }

    @Builder
    @Getter
    public static class AnimeVoteHistoryDto {
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

        Integer year;
        Integer quarter;
        Medium medium;

        SurveyCommentDto surveyCommentDto;
    }

    @Builder
    @Getter
    public static class AnimeCandidateListDto {
        Integer year;
        SurveyType type;

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

        Integer year;
        Integer quarter;
        Medium medium;
    }
}
