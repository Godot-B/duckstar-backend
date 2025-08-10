package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    // caffeinate
    @Operation(summary = "홈페이지 초기 데이터 조회 API",  description =
            """
                    투표가 완료된 지난 주 기준
                    1. Hot 급상승 애니/캐릭터 리스트 (각 최소 2개, 합 최대 6개)
                    2. 애니/남캐/여캐 TOP 10: 애니(덕스타 & Anime Trend, AniLab), 캐릭터(덕스타 & Anime Trend)
                    3. 드롭다운용 최근 12주 year, quarter, week""")
    @GetMapping("")
    public ApiResponse<Void> getHome() {
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "주차별 TOP 10 조회 API",  description =
            """
                    path variable 에 해당하는 주차 기준
                    애니(덕스타 & Anime Trend, AniLab), 캐릭터(덕스타 & Anime Trend)""")
    @GetMapping("/{year}/{quarter}/{week}")
    public ApiResponse<Void> getTop10ByWeek(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week
    ) {

        return ApiResponse.onSuccess(null);
    }
}