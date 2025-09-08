package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class EpisodeDto {
    Long episodeId;

    Integer episodeNumber;

    Boolean isBreak;

    Boolean isRescheduled;

    LocalDateTime scheduledAt;

    LocalDateTime nextEpScheduledAt;
}
