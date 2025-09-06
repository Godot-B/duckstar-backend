package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class EpisodeDto {
    Integer episodeNumber;

    Boolean isBreak;

    LocalDateTime scheduledAt;

    Boolean isRescheduled;

    LocalDateTime nextEpScheduledAt;
}
