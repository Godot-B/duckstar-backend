package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.service.AnimeService;
import com.duckstar.service.CommentService;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.CommentRequestDto;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import com.duckstar.web.dto.CommentResponseDto.DeleteResultDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/animes")
@RequiredArgsConstructor
@Validated
public class AnimeController {

    private final AnimeService animeService;
    private final CommentService commentService;

    @Operation(summary = "애니메이션 홈 조회 API")
    @GetMapping("/{animeId}")
    public ApiResponse<AnimeHomeDto> getAnimeHomeById(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(
                animeService.getAnimeHomeDtoById(animeId));
    }

//    @Operation(summary = "애니메이션 등장인물 전체 조회 API")
//    @GetMapping("/{animeId}/characters")

    @Operation(summary = "애니메이션 댓글 작성 API")
    @PostMapping("/{animeId}")
    public ApiResponse<CommentDto> leaveComment(
            @PathVariable Long animeId,
            @RequestBody CommentRequestDto request,
            @AuthenticationPrincipal(expression = "id") Long memberId
    ) {
        return ApiResponse.onSuccess(
                commentService.leaveAnimeComment(
                        animeId,
                        request,
                        memberId
                )
        );
    }
}
