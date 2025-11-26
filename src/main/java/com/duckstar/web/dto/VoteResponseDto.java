package com.duckstar.web.dto;

import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.EpisodeStar;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class VoteResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CandidateFormDto {  // 후보 모달용
        Long episodeId;

        Integer voterCount;

        Long animeId;

        String mainThumbnailUrl;

        VoteFormResultDto result;
    }

    @Getter
    @AllArgsConstructor
    public static class WeekCandidateDto {  // 주차 후보
        Long episodeId;

        EpEvaluateState state;

        Boolean hasVoted;

        String mainThumbnailUrl;

        String titleKor;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LiveCandidateListDto {  // 실시간 투표
        WeekDto weekDto;

        List<LiveCandidateDto> currentWeekLiveCandidates;

        List<LiveCandidateDto> lastWeekLiveCandidates;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class LiveCandidateDto {
        /**
         * 후보(에피소드) 정보
         */
        Integer year;
        Integer quarter;
        Integer week;

        Long episodeId;

        /**
         * 애니 정보
         */
        Long animeId;

        String mainThumbnailUrl;

        String titleKor;

        DayOfWeekShort dayOfWeek;

        LocalDateTime scheduledAt;

        String airTime;     // 방영시간 (HH:mm 형식)

        String genre;

        Medium medium;

        /**
         * 유저 기록
         */
        VoteResultDto result;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class VoteFormResultDto {
        Boolean isLateParticipating;

        Integer voterCount;

        StarInfoDto info;

        LocalDateTime voteUpdatedAt;

        Long commentId;

        String body;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class VoteResultDto {
        Integer voterCount;

        StarInfoDto info;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StarInfoDto {
        Boolean isBlocked;

        Long episodeStarId;  // 추가
        Integer userStarScore;

        Double starAverage;

        Integer star_0_5;
        Integer star_1_0;
        Integer star_1_5;
        Integer star_2_0;
        Integer star_2_5;
        Integer star_3_0;
        Integer star_3_5;
        Integer star_4_0;
        Integer star_4_5;
        Integer star_5_0;

        public static StarInfoDto of(
                Boolean isBlocked,
                EpisodeStar episodeStar,
                Episode episode
        ) {
            if (episode == null) {
                return StarInfoDto.builder().build();
            }

            return StarInfoDto.builder()
                    .isBlocked(isBlocked)
                    .episodeStarId(episodeStar.getId())
                    .userStarScore(episodeStar.getStarScore())
                    .starAverage(episode.getStarAverage())
                    .star_0_5(episode.getStar_0_5())
                    .star_1_0(episode.getStar_1_0())
                    .star_1_5(episode.getStar_1_5())
                    .star_2_0(episode.getStar_2_0())
                    .star_2_5(episode.getStar_2_5())
                    .star_3_0(episode.getStar_3_0())
                    .star_3_5(episode.getStar_3_5())
                    .star_4_0(episode.getStar_4_0())
                    .star_4_5(episode.getStar_4_5())
                    .star_5_0(episode.getStar_5_0())
                    .build();
        }
    }


    /**
     * legacy
     */
    @Builder
    @Getter
    public static class AnimeVoteHistoryDto {
        Boolean hasVoted;

        Long memberId;

        String nickName;

        Long submissionId;

        WeekDto weekDto;

        ContentType category;

        Integer normalCount;

        Integer bonusCount;

        LocalDateTime submittedAt;

        List<AnimeBallotDto> animeBallotDtos;

        public static AnimeVoteHistoryDto ofEmpty(Long memberId) {
            return AnimeVoteHistoryDto.builder()
                    .hasVoted(false)
                    .memberId(memberId)
                    .build();
        }
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
        Long weekId;

        WeekDto weekDto;

        List<AnimeCandidateDto> animeCandidates;
        Integer candidatesCount;

        Gender memberGender;
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
