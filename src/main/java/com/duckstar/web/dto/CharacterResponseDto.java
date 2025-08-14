package com.duckstar.web.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class CharacterResponseDto {

    @Builder
    @Getter
    public static class CharacterRankPreviewDto {

    }

    @Builder
    @Getter
    public static class CharacterRankDto {

    }

    @Builder
    @Getter
    public static class CharacterStatDto {
        // 데뷔 순위 없다

        Integer peakRank;

        LocalDate peakDate;

        Integer weeksOnTop10;
    }
}
