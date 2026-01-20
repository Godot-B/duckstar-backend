package com.duckstar.web.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class EpisodeRequestDto {

    @Builder
    @Getter
    public static class TotalEpisodesRequestDto {
        @NotNull
        @Min(1)
        @Max(100)
        Integer totalEpisodes;
    }

    @Builder
    @Getter
    public static class ModifyRequestDto {
        Integer episodeNumber;

        LocalDateTime rescheduledAt;
    }
}
