package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

public class VoteResponseDto {

    @Builder
    @Getter
    public static class VoteRatioDto {
        Double votePercent;

        Double malePercent;

        Double femalePercent;
    }
}
