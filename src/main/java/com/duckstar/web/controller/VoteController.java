package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.domain.Member;
import com.duckstar.service.VoteService;
import com.duckstar.web.dto.VoteRequestDto.AnimeVoteRequest;
import com.duckstar.web.dto.VoteResponseDto.AnimeCandidateListDto;
import com.duckstar.web.dto.VoteResponseDto.AnimeVoteHistoryDto;
import com.duckstar.web.support.VoteCookieManager;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "애니메이션 투표 API")
    @PostMapping("/anime")
    public ApiResponse<VoteReceiptDto> voteAnime(
            @RequestBody AnimeVoteRequest request,
            Member member,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        String cookieId = voteCookieManager.ensureVoteCookie(requestRaw, responseRaw);
        String principalKey = voteCookieManager.toPrincipalKey(member.getId(), cookieId);

        return ApiResponse.onSuccess(
                voteService.voteAnime(
                        request,
                        member,
                        cookieId,
                        principalKey
                )
        );
    }

    @Operation(summary = "애니메이션 투표 내역 조회 API")
    @GetMapping("/anime/history")
    public ApiResponse<AnimeVoteHistoryDto> getAnimeVoteHistory(
            Long memberId,
            @CookieValue(name = "vote_cookie_id", required = false) String cookieId
    ) {
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        return ApiResponse.onSuccess(
                voteService.getAnimeVoteHistory(principalKey));
    }
}
