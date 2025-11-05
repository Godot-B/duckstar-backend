package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.domain.enums.CommentSortType;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.AnimeService;
import com.duckstar.service.CommentService;
import com.duckstar.web.dto.AnimeResponseDto.AnimeHomeDto;
import com.duckstar.web.dto.BoardRequestDto.CommentRequestDto;
import com.duckstar.web.dto.CommentResponseDto.AnimeCommentSliceDto;
import com.duckstar.web.dto.CommentResponseDto.CommentDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;

@RestController
@RequestMapping("/api/v1/animes")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;
    private final CommentService commentService;
    private final EpisodeRepository episodeRepository;

    @Operation(summary = "애니메이션 댓글 조회 API")
    @GetMapping("/{animeId}/comments")
    public ApiResponse<AnimeCommentSliceDto> getAnimeComments(
            @PathVariable Long animeId,
            @RequestParam(required = false) List<Long> episodeIds,
            @RequestParam(defaultValue = "RECENT") CommentSortType sortBy,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.getAnimeCommentSliceDto(
                        animeId,
                        episodeIds,
                        sortBy,
                        pageable,
                        principal
                ));
    }

    @Operation(summary = "애니메이션 댓글 작성 API")
    @PostMapping("/{animeId}")
    public ApiResponse<CommentDto> leaveComment(
            @PathVariable Long animeId,
            @Valid @ModelAttribute CommentRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        return ApiResponse.onSuccess(
                commentService.leaveAnimeComment(
                        animeId,
                        request,
                        principal
                )
        );
    }

    @Operation(summary = "애니메이션 홈 조회 API")
    @GetMapping("/{animeId}")
    public ApiResponse<AnimeHomeDto> getAnimeHomeById(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(
                animeService.getAnimeHomeDtoById(animeId));
    }

    @Operation(summary = "애니메이션 에피소드 조회 API")
    @GetMapping("/{animeId}/episodes")
    public ApiResponse<List<EpisodeDto>> getEpisodesByAnime(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(
                episodeRepository.getEpisodeDtosByAnimeId(animeId));
    }

    @Operation(summary = "모든 애니메이션 ID 조회 API (SEO용)")
    @GetMapping("/ids")
    public ApiResponse<List<Long>> getAllAnimeIds() {
        return ApiResponse.onSuccess(animeService.getAllAnimeIds());
    }

//    @Operation(summary = "애니메이션 등장인물 전체 조회 API")
//    @GetMapping("/{animeId}/characters")

//    public ResponseEntity<Void> updateAnimeOtt() {
//
//    }
}
