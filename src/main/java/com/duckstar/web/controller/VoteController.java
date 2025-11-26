package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.service.EpisodeQueryService;
import com.duckstar.service.VoteCommandServiceImpl;
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

    private final VoteCommandServiceImpl voteCommandServiceImpl;
    private final EpisodeQueryService episodeQueryService;

    /**
     * 별점 투표 방식
     */

    @Operation(summary = "실시간 투표 리스트 조회 API",
            description = "now - 36시간 ~ now 범위의 에피소드들 (VOTING_WINDOW 상태) 조회")
    @GetMapping("/star")
    public ApiResponse<LiveCandidateListDto> getLiveCandidates(
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(
                episodeQueryService.getLiveCandidatesByWindow(memberId, requestRaw));
    }

    @Operation(summary = "실시간 투표/수정 API (비로그인 허용)",
            description = "TVA 투표: Episode 기반, 방송 후 36시간 동안 오픈.")
    @PostMapping("/star")
    public ApiResponse<VoteResultDto> voteOrUpdateStar(
            @Valid @RequestBody StarRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(voteCommandServiceImpl.voteOrUpdate(
                request,
                memberId,
                requestRaw,
                responseRaw
        ));
    }

    @Operation(summary = "주차 후보 목록 조회 API")
    @GetMapping("/episodes/{year}/{quarter}/{week}")
    public ApiResponse<List<WeekCandidateDto>> getWeekCandidatesByYQW(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(episodeQueryService.getWeekCandidatesByYQW(
                        year,
                        quarter,
                        week,
                        memberId,
                        requestRaw
                )
        );
    }

    @Operation(summary = "후보 단건 조회 API", description = "단일 후보 모달에 사용")
    @GetMapping("/episodes/{episodeId}")
    public ApiResponse<CandidateFormDto> getEpisode(
            @PathVariable Long episodeId,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(episodeQueryService.getCandidateForm(
                        episodeId,
                        memberId,
                        requestRaw
                )
        );
    }

    @Operation(summary = "투표 폼(모달) 투표/수정 API (로그인 ONLY)",
            description = "TVA 투표 : Episode 기반, " +
                    "방송 후 투표시간부터 주차 마감 전까지, Comment 5글자 이상 필수")
    @PostMapping("/star-form")
    public ApiResponse<VoteFormResultDto> voteOrUpdateWithStarForm(
            @Valid @RequestBody LateStarRequestDto request,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw
    ) {
        Long memberId = principal == null ? null : principal.getId();

        return ApiResponse.onSuccess(voteCommandServiceImpl.voteOrUpdateWithLoginAndComment(
                request,
                memberId,
                requestRaw
        ));
    }

    // 개발 연기
//    @Operation(summary = "상시 평가/수정 API (로그인 ONLY)",
//            description = "Episode 기반, 지난 주차")
//    @PostMapping()

    @Operation(summary = "별점 회수 API", description = "starScore 를 null 로 셋팅")
    @PostMapping("/withdraw/{episodeId}/{episodeStarId}")
    public ApiResponse<Void> withdrawStar(
            @PathVariable Long episodeId,
            @PathVariable Long episodeStarId,
            @AuthenticationPrincipal MemberPrincipal principal,
            HttpServletRequest requestRaw,
            HttpServletResponse responseRaw) {

        Long memberId = principal == null ? null : principal.getId();

        voteCommandServiceImpl.withdrawVote(
                episodeId,
                episodeStarId,
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
