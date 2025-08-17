package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BallotType {
    NORMAL(1), BONUS(2);

    private final int order;
}
