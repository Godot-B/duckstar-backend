package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.EpisodeQueryService;
import com.duckstar.service.VoteCommandService;
import com.duckstar.web.support.IdentifierExtractor;
import com.duckstar.web.support.Hasher;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.duckstar.web.dto.VoteRequestDto.*;
import static com.duckstar.web.dto.VoteResponseDto.*;

@RestController
@RequestMapping("/api/v1/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteCommandService voteCommandService;
    private final EpisodeQueryService episodeQueryService;

    /**
     * 별점 투표 방식
     */

    @Operation(summary = "실시간 투표 리스트 조회 API", description = "now - 36시간 ~ now 범위의 에피소드들 (VOTING_WINDOW 상태) 조회")
    @GetMapping("/star")
    public ApiResponse<StarCandidateListDto> getStarCandidates(
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw) {

        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(
                episodeQueryService.getStarCandidatesByWindow(memberId, requestRaw));
    }

    @Operation(summary = "실시간 투표/수정 API (비로그인 허용)",
            description = "TVA 투표: Episode 기반, 방송 후 36시간 동안 오픈.")
    @PostMapping("/star")
    public ApiResponse<StarInfoDto> voteOrUpdateStar(
            @Valid @RequestBody StarRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(voteCommandService.voteOrUpdateStar(
                request,
                memberId,
                requestRaw,
                responseRaw
        ));
    }

    // 마지막 후보 투표시간 끝나고 ~ 주차 발표 전까지 공백 ??

    @Operation(summary = "늦참 투표/수정 API (로그인 ONLY)",
            description = "TVA 투표 : Episode 기반, " +
                    "방송 후 투표시간 끝나고 주차 마감 전까지, Comment 5글자 이상 필수")
    @PostMapping("/star-late")
    public ApiResponse<StarInfoDto> lateVoteOrUpdateStar(
            @Valid @RequestBody LateStarRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        return ApiResponse.onSuccess(voteCommandService.lateVoteOrUpdateStar(
                request,
                principal,
                requestRaw,
                responseRaw
        ));
    }

    // 개발 연기
//    @Operation(summary = "상시 평가/수정 API (로그인 ONLY)",
//            description = "Episode 기반, 지난 주차")
//    @PostMapping()

    @Operation(summary = "별점 회수 API", description = "starScore 를 null 로 셋팅")
    @PostMapping("/withdraw/{episodeId}")
    public ApiResponse<Void> withdrawStar(
            @PathVariable Long episodeId,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw) {

        Long memberId = principal == null ? null : principal.getId();

        voteCommandService.withdrawStar(
                episodeId,
                memberId,
                requestRaw,
                responseRaw
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
//    @Operation(summary = "(leagcy) 애니메이션 투표 (일반 방식) API")
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
