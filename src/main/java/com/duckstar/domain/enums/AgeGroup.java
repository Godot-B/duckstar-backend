package com.duckstar.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeGroup {
    UNDER_14("14 이하", 0, 14),
    AGE_15_19("15-19", 15, 19),
    AGE_20_24("20-24", 20, 24),
    AGE_25_29("25-29", 25, 29),
    AGE_30_34("30-34", 30, 34),
    OVER_35("35 이상", 35, 150); // 상한선은 적절히 설정

    private final String label;   // UI에 보여줄 텍스트
    private final int minAge;     // 로직 계산용 최소 나이
    private final int maxAge;     // 로직 계산용 최대 나이
}