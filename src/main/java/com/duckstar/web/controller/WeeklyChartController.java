package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.ChartDto;
import com.duckstar.web.dto.ChartDto.AnimeRankSlice_legacyDto;
import com.duckstar.web.dto.WeekResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.duckstar.web.dto.ChartDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;

@RestController
@RequestMapping("/api/v1/chart")
@RequiredArgsConstructor
@Validated
public class WeeklyChartController {

    private final WeekService weekService;

    @Operation(summary = "모든 주차 조회 API")
    @GetMapping("/weeks")
    public ApiResponse<List<WeekDto>> getAllWeeks() {
        return ApiResponse.onSuccess(weekService.getAllWeeks());
    }

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
