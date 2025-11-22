package com.duckstar.web.dto;

import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.Gender;
import com.duckstar.validation.annotation.AnimeVoteConstraint;
import jakarta.validation.Valid;
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
    }

    @Getter
    public static class StarRequestDto {
        @NotNull
        Long episodeId;

        @NotNull
        @Min(1)
        @Max(10)
        Integer starScore;
    }

    @AnimeVoteConstraint
    @Getter
    public static class AnimeVoteRequest {
        @NotNull
        Long weekId;

        @NotNull
        Gender gender;

        @Valid
        @NotEmpty
        List<BallotRequestDto> ballotRequests;
    }
    
    @Getter
    public static class BallotRequestDto {
        @NotNull
        Long candidateId;

        @NotNull
        BallotType ballotType;
    }

    @Getter
    public static class AnimeRevoteRequest {
        @NotNull
        Long weekId;

        @NotNull
        Gender gender;

        List<BallotRequestDto> added;
        List<BallotRequestDto> removed;
        List<BallotRequestDto> updated;
    }
}
