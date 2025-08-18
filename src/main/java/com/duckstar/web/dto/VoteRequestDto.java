package com.duckstar.web.dto;

import com.duckstar.domain.enums.BallotType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class VoteRequestDto {

    @Getter
    public static class AnimeVoteRequest {
        Long weekId;

        List<AnimeBallotDto> ballotDtos;
    }
    
    @Getter
    public static class AnimeBallotDto {
        Long animeCandidateId;

        BallotType ballotType;
    }
}
