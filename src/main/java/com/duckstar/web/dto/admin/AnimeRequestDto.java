package com.duckstar.web.dto.admin;

import com.duckstar.domain.enums.AnimeStatus;
import com.duckstar.domain.enums.DayOfWeekShort;
import com.duckstar.domain.enums.Medium;
import com.duckstar.web.dto.OttDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AnimeRequestDto {

    @Builder
    @Getter
    @Setter  // ModelAttribute 는 세터 필수
    public static class PostRequestDto {

        @NotNull
        String titleKor;
        String titleOrigin;
        String titleEng;

        @NotNull
        Medium medium;

        LocalDateTime premiereDateTime;  // 입력 시 프론트에서 아래 요일에 우선 채워줌
        DayOfWeekShort dayOfWeek;
        LocalTime airTime;     // 문자열, 입력 시 프론트에서 아래 방영일에 우선 채워줌
        Integer totalEpisodes;

        String corp;
        String director;
        String genre;
        String author;  // 원작
        Integer minAge;  // 시청 등급
        String officialSiteString;
        String synopsis;

        MultipartFile mainImage;

        List<OttDto> ottDtos;
    }

    @Getter
    @Setter  // ModelAttribute 는 세터 필수
    public static class ImageRequestDto {
        MultipartFile mainImage;
    }

    @Builder
    @Getter
    public static class TotalEpisodesRequestDto {
        @NotNull
        @Min(1)
        @Max(100)
        Integer totalEpisodes;
    }

    @Builder
    @Getter
    public static class InfoRequestDto {
        DayOfWeekShort dayOfWeek;
        LocalTime airTime;

        AnimeStatus status;

        String corp;
    }
}
