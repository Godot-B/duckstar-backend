package com.duckstar.web.dto;

import com.duckstar.domain.mapping.weeklyVote.Episode;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class EpisodeResponseDto {

    @Builder
    @Getter
    public static class EpisodeDto {

        Long episodeId;

        Integer episodeNumber;

        Boolean isBreak;

        Boolean isRescheduled;

        LocalDateTime scheduledAt;

        LocalDateTime nextEpScheduledAt;
    }

    @Builder
    @Getter
    public static class EpisodePreviewDto {
        Long episodeId;

        Integer episodeNumber;

        LocalDateTime scheduledAt;

        public static EpisodePreviewDto of(Episode episode) {
            return EpisodePreviewDto.builder()
                    .episodeId(episode.getId())
                    .episodeNumber(episode.getEpisodeNumber())
                    .scheduledAt(episode.getScheduledAt())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class EpisodeResultDto {
        String message;

        List<EpisodePreviewDto> addedEpisodes;

        List<EpisodePreviewDto> deletedEpisodes;
    }
}
