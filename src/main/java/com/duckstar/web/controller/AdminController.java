package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.AnimeService;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final AnimeService animeService;

    @Operation(summary = "애니메이션 총 화수 수정 API")
    @PostMapping("/{animeId}/total-episodes")
    public ApiResponse<EpisodeResultDto> updateTotalEpisodes(
            @PathVariable Long animeId, @Valid @RequestBody EpisodeRequestDto request) {
        return ApiResponse.onSuccess(animeService.updateTotalEpisodes(animeId, request));
    }

    @Operation(summary = "애니메이션 총 화수 알 수 없음 Set API")
    @PatchMapping("/{animeId}/total-episodes/unknown")
    public ApiResponse<EpisodeResultDto> updateTotalEpisodes(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(animeService.setUnknown(animeId));
    }

    @Operation(summary = "애니메이션 등록 API")
    @PostMapping("/animes")
    public ApiResponse<Long> addAnime(@Valid @ModelAttribute PostRequestDto request) throws IOException {
        return ApiResponse.onSuccess(animeService.addAnime(request));
    }

    @Operation(summary = "애니메이션 메인 이미지 수정 API")
    @PostMapping("/animes/{animeId}")
    public ApiResponse<Long> updateAnimeImage(@PathVariable Long animeId,
                                      @ModelAttribute ImageRequestDto request) throws IOException {
        return ApiResponse.onSuccess(animeService.updateAnimeImage(animeId, request));
    }

    @Operation(summary = "에피소드 휴방 API")
    // 방영시간(필드) 수정, 에피소드 생성(삽입)
    @PostMapping("/animes/{animeId}/{episodeId}")
    public ApiResponse<Void> breakEpisode(@PathVariable Long animeId,
                                          @PathVariable Long episodeId) {

        return ApiResponse.onSuccess(null);
    }
}
