package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OttType {

    LAFTEL(1),
    NETFLIX(2),
    WAVVE(3),
    TVING(4),
    WATCHA(5),
    PRIME(6);

    private final int order;
}