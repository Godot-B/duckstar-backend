package com.duckstar.web.dto;

import com.duckstar.domain.enums.VoteType;
import lombok.Builder;
import lombok.Getter;

public class VoteRequestDto {

    @Builder
    @Getter
    public static class AnimeVoteRequestDto {
        Long animeCandidateId;

        Long memberId;

        VoteType voteType;
    }
}
