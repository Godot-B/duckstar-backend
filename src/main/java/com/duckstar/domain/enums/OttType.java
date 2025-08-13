package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OttType {

    LAFTEL(1), NETFLIX(2), PRIME(3), TVING(4), WAVVE(5), WATCHA(6);

    private final int order;
}