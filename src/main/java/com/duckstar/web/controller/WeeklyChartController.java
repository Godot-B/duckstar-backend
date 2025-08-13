package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.web.dto.ChartDto;
import com.duckstar.web.dto.QuarterDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
@Validated
public class WeeklyChartController {

    @Operation(summary = "주차별 애니메이션 차트 슬라이스 조회 API (with Anime Trend)",
            description = "path variable 해당 주차 애니, Anime Trend 커서 기반 무한 스크롤")
    @GetMapping("/{year}/{quarter}/{week}/anime")
    public ApiResponse<ChartDto.AnimeRankSliceDto> getWeeklyAnimeChartWithAnimeTrend(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 AniLab 차트 슬라이스 조회 API",
            description = "프론트 탭 전환용: path variable 해당 주차 AniLab 커서 기반 무한 스크롤")
    @GetMapping("/{year}/{quarter}/{week}/anime/with-lab")
    public ApiResponse<ChartDto.AniLabRankSliceDto> getWeeklyAnimeChartWithAniLab(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 남캐 차트 슬라이스 조회 API (with Anime Trend)",
            description = "path variable 해당 주차 남캐, Anime Trend 커서 기반 무한 스크롤")
    @GetMapping("/{year}/{quarter}/{week}/hero")
    public ApiResponse<ChartDto.CharacterRankSliceDto> getWeeklyHeroChart(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {

        return ApiResponse.onSuccess(null);
    }
    @Operation(summary = "주차별 여캐 차트 슬라이스 조회 API (with Anime Trend)",
            description = "path variable 해당 주차 여캐, Anime Trend 커서 기반 무한 스크롤")
    @GetMapping("/{year}/{quarter}/{week}/heroine")
    public ApiResponse<ChartDto.CharacterRankSliceDto> getWeeklyHeroineChart(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {

        return ApiResponse.onSuccess(null);
    }
}
