package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.AnimeService;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.EpisodeResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final AnimeService animeService;

    @Operation(summary = "애니메이션 총 화수 수정 API")
    @PostMapping(value = "/{animeId}/total-episodes")
    public ApiResponse<EpisodeResultDto> updateTotalEpisodes(
            @PathVariable Long animeId, @RequestBody EpisodeRequestDto request) {
        return ApiResponse.onSuccess(animeService.updateTotalEpisodes(animeId, request));
    }

    @Operation(summary = "애니메이션 총 화수 알 수 없음 Set API")
    @PatchMapping(value = "/{animeId}/total-episodes/unknown")
    public ApiResponse<EpisodeResultDto> updateTotalEpisodes(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(animeService.setUnknown(animeId));
    }
}
