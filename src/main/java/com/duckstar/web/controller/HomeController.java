package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.HomeService;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.HomeDto;
import com.duckstar.web.dto.HomeDto.WeeklyTopDto;
import com.duckstar.web.dto.SummaryDto.RankSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
@Validated
public class HomeController {

    private final HomeService homeService;
    private final WeekService weekService;

    // caffeinate 필요 ?
    @Operation(summary = "홈페이지 초기 데이터 조회 API", description =
            """
                    투표가 완료된 지난 주차
                    1. 애니 & Anime Trend TOP N개
                    2. Hot 급상승 애니/캐릭터 리스트 (각 최소 2개, 합 최대 6개)
                    3. 드롭다운용 최근 12주""")
    @GetMapping("")
    public ApiResponse<HomeDto> getHome(
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50) int size
    ) {
        return ApiResponse.onSuccess(homeService.getHome(size));
    }

    @Operation(summary = "주차별 애니메이션 TOP N개 조회 API (with Anime Trend)", description =
            """
                    path variable 해당 주차
                    애니 & Anime Trend TOP N개""")
    @GetMapping("/{year}/{quarter}/{week}/anime")
    public ApiResponse<WeeklyTopDto> getAnimeTopNByWeek(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50) int size
    ) {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return ApiResponse.onSuccess(
                homeService.getAnimeWeeklyTopDto(weekId, size));
    }

    @Operation(summary = "주차별 AniLab TOP N개 조회 API",
            description = "프론트 탭 전환용: path variable 해당 주차 AniLab TOP N개")
    @GetMapping("/{year}/{quarter}/{week}/anime/with-lab")
    public ApiResponse<RankSummaryDto> getWeeklyAniLab(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50) int size
    ) {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 남캐 TOP N개 조회 API (with Anime Trend)", description =
            """
                    path variable 해당 주차
                    남캐 & Anime Trend TOP N개""")
    @GetMapping("/{year}/{quarter}/{week}/hero")
    public ApiResponse<WeeklyTopDto> getHeroTopNByWeek(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50) int size
    ) {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 여캐 TOP N개 조회 API (with Anime Trend)", description =
            """
                    path variable 해당 주차
                    여캐 & Anime Trend TOP N개""")
    @GetMapping("/{year}/{quarter}/{week}/heroine")
    public ApiResponse<WeeklyTopDto> getHeroineTopNByWeek(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50) int size
    ) {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return ApiResponse.onSuccess(null);
    }
}