package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.ChartDto.AnimeRankSliceDto;
import com.duckstar.web.dto.ChartDto.CharacterRankSliceDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
@Validated
public class WeeklyChartController {

    private final WeekService weekService;

    @Operation(summary = "주차별 애니메이션 차트 슬라이스 조회 API (with Anime Trend, AniLab)",
            description = "path variable 해당 주차 애니, Anime Trend, AniLab 커서 기반 무한 스크롤")
    @GetMapping("/{year}/{quarter}/{week}/anime")
    public ApiResponse<AnimeRankSliceDto> getWeeklyAnimeChart(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return ApiResponse.onSuccess(
                weekService.getAnimeRankSliceDto(weekId, pageable));
    }
//
//    @Operation(summary = "주차별 남캐 차트 슬라이스 조회 API (with Anime Trend)",
//            description = "path variable 해당 주차 남캐, Anime Trend 커서 기반 무한 스크롤")
//    @GetMapping("/{year}/{quarter}/{week}/hero")
//    public ApiResponse<CharacterRankSliceDto> getWeeklyHeroChart(
//            @PathVariable Integer year,
//            @PathVariable Integer quarter,
//            @PathVariable Integer week,
//            @ParameterObject @PageableDefault(size = 20) Pageable pageable
//    ) {
//        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);
//
//        return ApiResponse.onSuccess(null);
//    }
//    @Operation(summary = "주차별 여캐 차트 슬라이스 조회 API (with Anime Trend)",
//            description = "path variable 해당 주차 여캐, Anime Trend 커서 기반 무한 스크롤")
//    @GetMapping("/{year}/{quarter}/{week}/heroine")
//    public ApiResponse<CharacterRankSliceDto> getWeeklyHeroineChart(
//            @PathVariable Integer year,
//            @PathVariable Integer quarter,
//            @PathVariable Integer week,
//            @ParameterObject @PageableDefault(size = 20) Pageable pageable
//    ) {
//        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);
//
//        return ApiResponse.onSuccess(null);
//    }
}
