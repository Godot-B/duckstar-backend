package com.duckstar.web.controller;

import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AdminHandler;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.service.ShadowBanService;
import com.duckstar.service.AdminActionLogService;
import com.duckstar.service.AnimeService;
import com.duckstar.service.SubmissionService;
import com.duckstar.web.dto.admin.AdminLogDto;
import com.duckstar.web.dto.admin.AdminLogDto.IpManagementLogSliceDto;
import com.duckstar.web.dto.admin.EpisodeRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.duckstar.web.dto.EpisodeResponseDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;
import static com.duckstar.web.dto.admin.SubmissionResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final AnimeService animeService;
    private final ShadowBanService shadowBanService;
    private final SubmissionService submissionService;
    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;
    private final AdminActionLogService adminActionLogService;

    @Operation(summary = "애니메이션 총 화수 수정 API")
    @PostMapping("/{animeId}/total-episodes")
    public ApiResponse<EpisodeResultDto> updateTotalEpisodes(
            @PathVariable Long animeId, @Valid @RequestBody EpisodeRequestDto request) {
        return ApiResponse.onSuccess(animeService.updateTotalEpisodes(animeId, request));
    }

    @Operation(summary = "애니메이션 총 화수 알 수 없음 Set API")
    @PatchMapping("/{animeId}/total-episodes/unknown")
    public ApiResponse<EpisodeResultDto> updateTotalEpisodes(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(animeService.setUnknown(animeId));
    }

    @Operation(summary = "애니메이션 등록 API")
    @PostMapping("/animes")
    public ApiResponse<Long> addAnime(@Valid @ModelAttribute PostRequestDto request) throws IOException {
        return ApiResponse.onSuccess(animeService.addAnime(request));
    }

    @Operation(summary = "애니메이션 메인 이미지 수정 API")
    @PostMapping("/animes/{animeId}")
    public ApiResponse<Long> updateAnimeImage(@PathVariable Long animeId,
                                      @ModelAttribute ImageRequestDto request) throws IOException {
        return ApiResponse.onSuccess(animeService.updateAnimeImage(animeId, request));
    }

    @Operation(summary = "에피소드 휴방 API")
    // 방영시간(필드) 수정, 에피소드 생성(삽입)
    @PostMapping("/animes/{animeId}/{episodeId}")
    public ApiResponse<Void> breakEpisode(@PathVariable Long animeId,
                                          @PathVariable Long episodeId) {

        return ApiResponse.onSuccess(null);
    }

    /**
     * 제출 현황 관리
     */
    @Operation(summary = "IP 관리 로그 조회 API", description = "커서 기반 무한 스크롤")
    @GetMapping("/submissions/logs")
    public ApiResponse<IpManagementLogSliceDto> getAdminLogsOnIpManagement(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.onSuccess(
                adminActionLogService.getIpManagementLogs(pageable));
    }

    @Operation(summary = "IP별 제출 수 슬라이스 조회 API", description = "커서 기반 무한 스크롤")
    @GetMapping("/submissions")
    public ApiResponse<SubmissionCountSliceDto> getSubmissionCountGroupByIp(
            @ParameterObject @PageableDefault(size = 50) Pageable pageable) {
        return ApiResponse.onSuccess(
                submissionService.getSubmissionCountGroupByIp(pageable));
    }

    @Operation(summary = "특정 주차, 특정 ip 제출 현황 전체 조회 API")
    @GetMapping("/ip")
    public ApiResponse<List<EpisodeStarDto>> getSubmissionsByWeekAndIp(
            @RequestParam Long weekId, @RequestParam String ipHash) {
        return ApiResponse.onSuccess(
                weekVoteSubmissionRepository.getEpisodeStarDtosByWeekIdAndIpHash(weekId, ipHash));
    }

    @Operation(summary = "ip 밴 토글 API")
    @PostMapping("/ip/ban")
    public ApiResponse<Void> banIp(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam String ipHash,
            @RequestParam Boolean enabled,
            @RequestParam @Size(max = 300) String reason
    ) {
        Long memberId = principal == null ? null : principal.getId();

        shadowBanService.setBanned(
                memberId,
                ipHash,
                enabled,
                reason
        );
        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "특정 주차, 특정 ip와 표 몰수 (전체 차단)")
    @PostMapping("/ip/withdraw")
    public ApiResponse<Void> withdrawVotesByWeekAndIp(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam Long weekId,
            @RequestParam String ipHash,
            @RequestParam @Size(max = 300) String reason
    ) {
        if (!shadowBanService.isBanned(ipHash)) {
            throw new AdminHandler(ErrorStatus.BAN_NOT_FOUND);
        }
        Long memberId = principal == null ? null : principal.getId();

        submissionService.withdrawSubmissions(
                memberId,
                weekId,
                ipHash,
                reason
        );

        return ApiResponse.onSuccess(null);
    }

    @Operation(summary = "되돌리기 - 특정 주차, 특정 ip와 표 몰수 롤백")
    @PostMapping("/ip/withdraw/undo")
    public ApiResponse<Void> undoWithdrawnSubmissions(
            @AuthenticationPrincipal MemberPrincipal principal,
            @RequestParam Long logId,
            @RequestParam Long weekId,
            @RequestParam String ipHash,
            @RequestParam @Size(max = 300) String reason
    ) {
        Long memberId = principal == null ? null : principal.getId();

        submissionService.undoWithdrawnSubmissions(
                memberId,
                logId,
                weekId,
                ipHash,
                reason
        );

        return ApiResponse.onSuccess(null);
    }
}
