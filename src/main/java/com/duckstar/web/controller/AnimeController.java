package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.repository.AnimeRepository;
import com.duckstar.service.AnimeService;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.MedalDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/animes")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;

    @Operation(summary = "애니메이션 홈 조회 API")
    @GetMapping("/{animeId}")
    public ApiResponse<AnimeHomeDto> getAnimeHomeById(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(
                animeService.getAnimeHomeDtoById(animeId));
    }

//    @Operation(summary = "애니메이션 등장인물 전체 조회 API")
//    @GetMapping("/{animeId}/characters")
}
