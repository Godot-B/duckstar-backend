package com.duckstar.web.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class EpisodeRequestDto {

    @Builder
    @Getter
    public static class ModifyRequestDto {
        Integer episodeNumber;

        LocalDateTime rescheduledAt;
    }
}
