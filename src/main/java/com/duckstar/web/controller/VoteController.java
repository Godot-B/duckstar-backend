package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.apiPayload.exception.GeneralException;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.VoteService;
import com.duckstar.web.dto.VoteRequestDto.AnimeVoteRequest;
import com.duckstar.web.dto.VoteResponseDto.AnimeCandidateListDto;
import com.duckstar.web.dto.VoteResponseDto.AnimeVoteHistoryDto;
import com.duckstar.web.support.VoteCookieManager;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.VoteResponseDto.*;

@RestController
@RequestMapping("/api/v1/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final VoteCookieManager voteCookieManager;

    @Operation(summary = "애니메이션 후보자 리스트 조회 API")
    @GetMapping("/anime")
    public ApiResponse<AnimeCandidateListDto> getAnimeCandidateList(
            @AuthenticationPrincipal MemberPrincipal principal) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                voteService.getAnimeCandidateList(memberId));
    }

    @Operation(summary = "애니 투표 참여 여부에 따른 투표 기록 조회 API")
    @GetMapping("/anime/status")
    public ApiResponse<AnimeVoteHistoryDto> getAnimeVoteStatus(
            @AuthenticationPrincipal MemberPrincipal principal,
            @CookieValue(name = "vote_cookie_id", required = false) String cookieId
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(
                voteService.getAnimeVoteHistory(memberId, cookieId));
    }

    @Operation(summary = "애니메이션 투표 API")
    @PostMapping("/anime")
    public ApiResponse<Void> voteAnime(
            @Valid @RequestBody AnimeVoteRequest request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        String cookieId = voteCookieManager.ensureVoteCookie(requestRaw, responseRaw);
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        voteService.voteAnime(
                request,
                memberId,
                cookieId,
                principalKey,
                requestRaw,
                responseRaw
        );

        return ApiResponse.onSuccess(null);
    }
}
