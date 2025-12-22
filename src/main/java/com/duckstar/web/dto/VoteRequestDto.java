package com.duckstar.web.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

public class VoteRequestDto {

    @Getter
    public static class LateStarRequestDto {
        @NotNull
        Long episodeId;

        @NotNull
        @Min(1)
        @Max(10)
        Integer starScore;

        @NotBlank
        @Size(min = 5, max = 1000)
        String body;

        Long episodeStarId;  // 추가
    }

    @Getter
    public static class StarRequestDto {
        @NotNull
        Long episodeId;

        @NotNull
        @Min(1)
        @Max(10)
        Integer starScore;

        Long episodeStarId;  // 추가
    }

    @Getter
    public static class SurveyCommentRequestDto {
        @NotNull
        Long animeId;

        @NotBlank
        @Size(min = 5, max = 1000)
        String body;

        Long candidateId;  // 추가
    }
}
