package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.VoteService;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.VoteRequestDto.AnimeVoteRequest;
import com.duckstar.web.dto.VoteResponseDto.AnimeCandidateListDto;
import com.duckstar.web.dto.VoteResponseDto.AnimeVoteHistoryDto;
import com.duckstar.web.support.IpExtractor;
import com.duckstar.web.support.IpHasher;
import com.duckstar.web.support.VoteCookieManager;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.duckstar.web.dto.VoteRequestDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

@RestController
@RequestMapping("/api/v1/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;
    private final VoteCookieManager voteCookieManager;
    private final IpHasher ipHasher;
    private final IpExtractor ipExtractor;

    /**
     * 별점 투표 방식
     */

    @Operation(summary = "별점 투표 후보자 리스트 조회 API", description = "now - 36시간 ~ now + 36시간 범위의 에피소드들 조회")
    @GetMapping("/star")
    public ApiResponse<StarCandidateListDto> getStarCandidates(
            @AuthenticationPrincipal MemberPrincipal principal,
            @CookieValue(name = "vote_cookie_id", required = false) String cookieId) {

        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(
                voteService.getStarCandidatesByWindow(memberId, cookieId));
    }

    @Operation(summary = "별점 투표/수정 API", description = "TVA 투표: Episode 기반, 방송 후 36시간 동안 오픈")
    @PostMapping("/star")
    public ApiResponse<StarInfoDto> voteOrUpdateStar(
            @Valid @RequestBody StarRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        String cookieId = voteCookieManager.ensureVoteCookie(requestRaw, responseRaw);

        String clientIp = ipExtractor.extract(requestRaw);
        String ipHash = ipHasher.hash(clientIp);

        return ApiResponse.onSuccess(voteService.voteOrUpdateStar(
                request,
                memberId,
                cookieId,
                ipHash
        ));
    }

    @Operation(summary = "별점 회수 API", description = "starScore 를 null 로 셋팅")
    @PostMapping("/withdraw/{episodeId}")
    public ApiResponse<Void> withdrawStar(
            @PathVariable Long episodeId,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw) {

        Long memberId = principal == null ? null : principal.getId();

        String cookieId = voteCookieManager.ensureVoteCookie(requestRaw, responseRaw);

        voteService.withdrawStar(
                episodeId,
                memberId,
                cookieId
        );

        return ApiResponse.onSuccess(null);
    }

//    /**
//     * 기존 투표 방식
//     */
//
//    @Operation(summary = "(legacy) 애니메이션 후보자 리스트 조회 API")
//    @GetMapping("/anime")
//    public ApiResponse<AnimeCandidateListDto> getAnimeCandidateList(
//            @AuthenticationPrincipal MemberPrincipal principal) {
//        Long memberId = principal == null ? null : principal.getId();
//        return ApiResponse.onSuccess(
//                voteService.getAnimeCandidateList(memberId));
//    }
//
//    @Operation(summary = "(legacy) 애니 투표 참여 여부에 따른 투표 기록 조회 API")
//    @GetMapping("/anime/status")
//    public ApiResponse<AnimeVoteHistoryDto> getAnimeVoteStatus(
//            @AuthenticationPrincipal MemberPrincipal principal,
//            @CookieValue(name = "vote_cookie_id", required = false) String cookieId
//    ) {
//        Long memberId = principal == null ? null : principal.getId();
//
//        return ApiResponse.onSuccess(
//                voteService.getAnimeVoteHistory(memberId, cookieId));
//    }
//
//    @Operation(summary = "(leagcy) 애니메이션 투표 (단일 방식) API")
//    @PostMapping("/anime")
//    public ApiResponse<Void> voteAnime(
//            @Valid @RequestBody AnimeVoteRequest request,
//            @AuthenticationPrincipal MemberPrincipal principal,
//            HttpServletRequest requestRaw,
//            HttpServletResponse responseRaw
//    ) {
//        Long memberId = principal == null ? null : principal.getId();
//
//        String cookieId = voteCookieManager.ensureVoteCookie(requestRaw, responseRaw);
//
//        String clientIp = ipExtractor.extract(requestRaw);
//        String ipHash = ipHasher.hash(clientIp);
//
//        voteService.voteAnime(
//                request,
//                memberId,
//                cookieId,
//                ipHash,
//                requestRaw,
//                responseRaw
//        );
//
//        return ApiResponse.onSuccess(null);
//    }
//
//    @Operation(summary = "(legacy) 애니메이션 재투표 API")
//    @PostMapping("/anime/{submissionId}")
//    public ApiResponse<Void> revoteAnime(
//            @PathVariable Long submissionId,
//            @Valid @RequestBody AnimeRevoteRequest request,
//            @AuthenticationPrincipal MemberPrincipal principal
//    ) {
//        Long memberId = principal == null ? null : principal.getId();
//
//        voteService.revoteAnime(submissionId, request, memberId);
//
//        return ApiResponse.onSuccess(null);
//    }
}
