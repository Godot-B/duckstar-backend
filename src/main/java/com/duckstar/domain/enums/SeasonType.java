package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeasonType {

    SPRING(1), SUMMER(2), AUTUMN(3), WINTER(4);

    private final int order;
}
