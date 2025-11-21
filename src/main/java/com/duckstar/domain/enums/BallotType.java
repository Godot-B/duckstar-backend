package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 일반 투표 방식의
 * 표 종류
 */
@Getter
@RequiredArgsConstructor
public enum BallotType {
    NORMAL(100), BONUS(50);

    private final int score;
}
