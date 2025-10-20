package com.duckstar.web.dto;

import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class VoteResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StarCandidateListDto {
        WeekDto weekDto;
        List<StarCandidateDto> starCandidates;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StarCandidateDto {
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

        AnimeStatus status;

        Boolean isBreak;    // TVA 결방 주 여부

        String titleKor;

        DayOfWeekShort dayOfWeek;

        Boolean isRescheduled;

        LocalDateTime scheduledAt;

        String airTime;     // 방영시간 (HH:mm 형식)

        String genre;

        Medium medium;

        /**
         * 유저 기록
         */
        StarInfoDto info;

        public void setUserHistory(StarInfoDto info) {
            this.info = info;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StarInfoDto {
        Integer userStarScore;

        Double starAverage;
        Integer voterCount;

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

        public static StarInfoDto of(Integer userStarScore, Episode episode) {
            if (episode == null) {
                return StarInfoDto.builder().build();
            }

            return StarInfoDto.builder()
                    .userStarScore(userStarScore)
                    .starAverage(episode.getRankInfo().getRankedAverage())
                    .voterCount(episode.getVoterCount())
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

        public static StarInfoDto createWithoutAvg(
                Integer voterCount,
                Integer star_0_5,
                Integer star_1_0,
                Integer star_1_5,
                Integer star_2_0,
                Integer star_2_5,
                Integer star_3_0,
                Integer star_3_5,
                Integer star_4_0,
                Integer star_4_5,
                Integer star_5_0
        ) {
            double weightedSum =
                    0.5 * star_0_5 +
                    1.0 * star_1_0 +
                    1.5 * star_1_5 +
                    2.0 * star_2_0 +
                    2.5 * star_2_5 +
                    3.0 * star_3_0 +
                    3.5 * star_3_5 +
                    4.0 * star_4_0 +
                    4.5 * star_4_5 +
                    5.0 * star_5_0;

            double starAverage = (voterCount == 0) ? 0.0 : weightedSum / (double) voterCount;

            return StarInfoDto.builder()
                    .starAverage(starAverage)
                    .voterCount(voterCount)
                    .star_0_5(star_0_5)
                    .star_1_0(star_1_0)
                    .star_1_5(star_1_5)
                    .star_2_0(star_2_0)
                    .star_2_5(star_2_5)
                    .star_3_0(star_3_0)
                    .star_3_5(star_3_5)
                    .star_4_0(star_4_0)
                    .star_4_5(star_4_5)
                    .star_5_0(star_5_0)
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

        VoteCategory category;

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
        VoteStatus status;

        Long weekId;

        WeekDto weekDto;

        List<AnimeCandidateDto> animeCandidates;
        Integer candidatesCount;

        Gender memberGender;

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
