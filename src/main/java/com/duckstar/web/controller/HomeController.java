package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.web.dto.HomeDto;
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

    // caffeinate 필요 ?
    @Operation(summary = "홈페이지 초기 데이터 조회 API", description =
            """
                    투표가 완료된 지난 주차
                    1. Hot 급상승 애니/캐릭터 리스트 (각 최소 2개, 합 최대 6개)
                    2. 애니/남캐/여캐 TOP N개, 애니만 AniLab 포함
                    3. 드롭다운용 최근 12주""")
    @GetMapping("")
    public ApiResponse<HomeDto> getHome(
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50)
            int size
    ) {

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 TOP N개 조회 API", description =
            """
                    path variable 해당 주차
                    애니/남캐/여캐 TOP N개, 애니만 AniLab 포함됨""")
    @GetMapping("/{year}/{quarter}/{week}")
    public ApiResponse<HomeDto.WeeklyTopDto> getTop10ByWeek(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @RequestParam(defaultValue = "10")
            @Min(1) @Max(50)
            int size
    ) {

        return ApiResponse.onSuccess(null);
    }
}