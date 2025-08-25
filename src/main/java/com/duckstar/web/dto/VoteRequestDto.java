package com.duckstar.web.dto;

import com.duckstar.domain.enums.BallotType;
import com.duckstar.domain.enums.Gender;
import com.duckstar.validation.annotation.AnimeVoteConstraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class VoteRequestDto {

    @AnimeVoteConstraint
    @Getter
    public static class AnimeVoteRequest {
        @NotNull
        Long weekId;

        @NotNull
        Gender gender;

        @Valid
        @NotEmpty
        List<AnimeBallotDto> ballotDtos;
    }
    
    @Getter
    public static class AnimeBallotDto {
        @NotNull
        Long animeCandidateId;

        @NotNull
        BallotType ballotType;
    }
}
