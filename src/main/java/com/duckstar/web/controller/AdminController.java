package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.crawler.reader.CsvImportService;
import com.duckstar.service.AnimeService;
import com.duckstar.web.dto.EpisodeResponseDto;
import com.duckstar.web.dto.admin.CsvRequestDto;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.duckstar.web.dto.EpisodeResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final CsvImportService csvImportService;
    private final AnimeService animeService;

    @PostMapping(value = "/import/{year}/{quarter}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importNewSeason(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @ModelAttribute CsvRequestDto request
    ) throws IOException {
        csvImportService.importNewSeason(year, quarter, request);
        return ResponseEntity.ok("✅ 데이터 import 성공");
    }

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
