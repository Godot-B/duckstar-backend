package com.duckstar.web.dto;

import com.duckstar.domain.mapping.weeklyVote.Episode;
import com.duckstar.util.QuarterUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.WeekResponseDto.*;

public class EpisodeResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class EpisodeDto {
        WeekDto weekDto;

        Long episodeId;

        Integer episodeNumber;

        Boolean isBreak;

        Boolean isRescheduled;

        LocalDateTime scheduledAt;

        LocalDateTime nextEpScheduledAt;

        public void setWeekDto(LocalDateTime scheduledAt) {
            weekDto = WeekDto.of(QuarterUtil.getThisWeekRecord(scheduledAt));
        }

        public static EpisodeDto of(Episode episode) {
            LocalDateTime scheduledAt = episode.getScheduledAt();
            WeekDto weekDto = WeekDto.of(QuarterUtil.getThisWeekRecord(scheduledAt));
            return EpisodeDto.builder()
                    .weekDto(weekDto)
                    .episodeId(episode.getId())
                    .episodeNumber(episode.getEpisodeNumber())
                    .isBreak(episode.isBreak())
                    .isRescheduled(episode.getIsRescheduled())
                    .scheduledAt(scheduledAt)
                    .nextEpScheduledAt(episode.getNextEpScheduledAt())
                    .build();
        }
    }

    @Builder
    @Getter
    public static class EpisodeResultDto {
        List<EpisodeDto> addedEpisodes;

        List<EpisodeDto> deletedEpisodes;
    }
}
