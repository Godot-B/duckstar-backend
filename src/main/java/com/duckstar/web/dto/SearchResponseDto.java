package com.duckstar.web.dto;

import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.domain.enums.SeasonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class SearchResponseDto {

    Integer size;
    List<AnimePreviewDto> animePreviews;

    @Builder
    @Getter
    public static class SeasonResponseDto {
        Integer year;
        List<SeasonType> types;
    }

    @Builder
    @Getter
    public static class AnimePreviewListDto {
        Integer year;
        Integer quarter;

        List<ScheduleDto> scheduleDtos;
    }

    @Builder
    @Getter
    public static class ScheduleDto {
        DayOfWeekShort dayOfWeekShort;

        List<AnimePreviewDto> animePreviews;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AnimePreviewDto {
        Long animeId;

        String titleKor;

        String mainThumbnailUrl;

        AnimeStatus status;

        Boolean isBreak;    // TVA 결방 주 여부

        Boolean isRescheduled;

        String genre;

        Medium medium;

        List<OttDto> ottDtos;

        // 분기 시간표에서는 정규 요일을 따르는 반면
        // * 주차 시간표에서는 각 에피소드의 요일을 따름
        DayOfWeekShort dayOfWeek;

        LocalDateTime scheduledAt;  // 영화는 개봉일 0시 0분

        // 주차 시간표는 필요 X
        // 오직 분기 시간표의 정규 시간 표시용
        LocalTime airTime;
    }

//    @Builder
//    @Getter
//    public static class CharacterPreviewDto {
//        Long characterId;
//
//        String mainThumbnailUrl;
//
//        String nameKor;
//
//        String cv;
//
//        String animeTitle;
//    }
}
