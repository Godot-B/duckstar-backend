package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BallotType {
    NORMAL(100), BONUS(50);

    private final int score;
}
