package com.duckstar.web.dto;

import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.web.dto.WeekResponseDto.WeekDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.duckstar.web.dto.AnimeResponseDto.*;

public class SearchResponseDto {

    @Builder
    @Getter
    public static class AnimePreviewListDto {
        WeekDto weekDto;

        Map<DayOfWeekShort, List<AnimePreviewDto>> schedule;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AnimePreviewDto {
        Long animeId;

        String mainThumbnailUrl;

        AnimeStatus status;

        Boolean isBreak;    // TVA 결방 주 여부

        String titleKor;

        DayOfWeekShort dayOfWeek;

        Boolean isRescheduled;

        LocalDateTime scheduledAt;

        String genre;

        Medium medium;

        List<OttDto> ottDtos;
    }

    @Builder
    @Getter
    public static class CharacterPreviewDto {
        Long characterId;

        String mainThumbnailUrl;

        String nameKor;

        String cv;

        String animeTitle;
    }
}
