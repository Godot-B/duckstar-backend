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
    public ApiResponse<AnimeCandidateListDto> getAnimeCandidateList() {
        return ApiResponse.onSuccess(
                voteService.getAnimeCandidateList());
    }

    @Operation(summary = "애니메이션 투표 참여 여부 확인 API", description = "dto의 필드 hasVoted가 false면 애니메이션 투표 API를, " +
            "true면 dto의 필드 submissionId를 통해 애니메이션 투표 내역 조회 API를 호출해주세요.")
    @GetMapping("/anime/check-voted")
    public ApiResponse<VoteCheckDto> checkVoted(
            @AuthenticationPrincipal MemberPrincipal principal,
            @CookieValue(name = "vote_cookie_id", required = false) String cookieId
    ) {
        Long memberId = principal == null ? null : principal.getId();

        String principalKey = null;
        try {
            principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);
        } catch (GeneralException e) {
            return ApiResponse.onSuccess(VoteCheckDto.of(null));
        }

        return ApiResponse.onSuccess(
                voteService.checkVoted(principalKey));
    }

    @Operation(summary = "애니메이션 투표 API")
    @PostMapping("/anime")
    public ApiResponse<VoteReceiptDto> voteAnime(
            @Valid @RequestBody AnimeVoteRequest request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        String cookieId = voteCookieManager.ensureVoteCookie(requestRaw, responseRaw);
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        return ApiResponse.onSuccess(
                voteService.voteAnime(
                        request,
                        memberId,
                        cookieId,
                        principalKey
                )
        );
    }

    @Operation(summary = "애니메이션 투표 내역 조회 API", description = "submissionId를 통해 투표 내역을 조회합니다.")
    @GetMapping("/anime/history/{submissionId}")
    public ApiResponse<AnimeVoteHistoryDto> getAnimeVoteHistory(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal MemberPrincipal principal,
            @CookieValue(name = "vote_cookie_id", required = false) String cookieId
    ) {
        Long memberId = principal == null ? null : principal.getId();

        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        return ApiResponse.onSuccess(
                voteService.getAnimeVoteHistory(submissionId, principalKey));
    }
}
