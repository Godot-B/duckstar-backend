package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    public ApiResponse<Void> getHome() {
        return ApiResponse.onSuccess(null);
    }

    public ApiResponse<Void> getTop10AnimesByWeek() {
        return ApiResponse.onSuccess(null);
    }

    public ApiResponse<Void> getTop10CharactersByWeek() {
        return ApiResponse.onSuccess(null);
    }

    public ApiResponse<Void> get11MoreLatestWeeks() {   // 드롭다운용
        return ApiResponse.onSuccess(null);
    }
}