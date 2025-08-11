package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.web.dto.ChartDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
public class WeeklyChartController {

    @Operation(summary = "주차별 애니메이션 차트 조회 API (with Anime Trend)", description =
            """
                    path variable 해당 주차
                    애니, Anime Trend""")
    @GetMapping("/anime/with-trend/{year}/{quarter}/{week}")
    public ApiResponse<ChartDto.AnimeRankSliceDto> getWeeklyAnimeChartWithAnimeTrend(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 애니메이션 차트 조회 API (with AniLab)", description =
            """
                    path variable 해당 주차
                    애니, AniLab""")
    @GetMapping("/anime/with-lab/{year}/{quarter}/{week}")
    public ApiResponse<ChartDto.AnimeRankSliceDto> getWeeklyAnimeChartWithAniLab(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 남캐 차트 조회 API (with Anime Trend)", description =
            """
                    path variable 해당 주차
                    남캐, Anime Trend""")
    @GetMapping("/hero/{year}/{quarter}/{week}")
    public ApiResponse<ChartDto.CharacterRankSliceDto> getWeeklyHeroChart(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.onSuccess(null);
    }
    @Operation(summary = "주차별 여캐 차트 조회 API (with Anime Trend)", description =
            """
                    path variable 해당 주차
                    여캐, Anime Trend""")
    @GetMapping("/heroine/{year}/{quarter}/{week}")
    public ApiResponse<ChartDto.CharacterRankSliceDto> getWeeklyHeroineChart(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {

        return ApiResponse.onSuccess(null);
    }
}
