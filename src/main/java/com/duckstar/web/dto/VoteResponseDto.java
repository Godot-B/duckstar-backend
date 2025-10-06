package com.duckstar.web.dto;

import com.duckstar.domain.Anime;
import com.duckstar.domain.enums.*;
import com.duckstar.domain.mapping.AnimeCandidate;
import com.duckstar.domain.mapping.AnimeVote;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class VoteResponseDto {

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

    @Getter
    @Builder
    @AllArgsConstructor
    public static class StarDistributionDto {
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

        public static StarDistributionDto of(Episode episode) {
            return StarDistributionDto.builder()
                    .starAverage(episode.getStarAverage())
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
    }
}
