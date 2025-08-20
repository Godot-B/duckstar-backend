package com.duckstar.web.dto;

import com.duckstar.domain.enums.BallotType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class VoteRequestDto {

    @Getter
    public static class AnimeVoteRequest {
        @NotNull
        Long weekId;

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
