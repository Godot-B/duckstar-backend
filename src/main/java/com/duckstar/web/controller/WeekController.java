package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.web.dto.QuarterDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/week")
@RequiredArgsConstructor
@Validated
public class WeekController {

    @Operation(summary = "특정 날짜로부터 모든 분기/주차 조회 API")
    @GetMapping("")
    public ApiResponse<List<QuarterDto>> getAllQWFromDate(LocalDate date) {

        return ApiResponse.onSuccess(null);
    }
}
