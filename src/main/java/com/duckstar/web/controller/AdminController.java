package com.duckstar.web.controller;

import com.duckstar.abroad.reader.CsvImportService;
import com.duckstar.apiPayload.ApiResponse;
import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.AdminHandler;
import com.duckstar.domain.enums.ManageFilterType;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.security.service.ShadowBanService;
import com.duckstar.service.AdminActionLogService;
import com.duckstar.service.AnimeService.AnimeCommandService;
import com.duckstar.service.AnimeService.AnimeQueryService;
import com.duckstar.service.ChartService;
import com.duckstar.service.EpisodeService.EpisodeCommandService;
import com.duckstar.service.EpisodeService.EpisodeQueryService;
import com.duckstar.service.SubmissionService;
import com.duckstar.service.WeekService;
import com.duckstar.web.dto.admin.AdminLogDto.ManagementLogSliceDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static com.duckstar.web.dto.admin.AdminLogDto.*;
import static com.duckstar.web.dto.admin.AnimeRequestDto.*;
import static com.duckstar.web.dto.admin.ContentResponseDto.*;
import static com.duckstar.web.dto.admin.CsvRequestDto.*;
import static com.duckstar.web.dto.admin.EpisodeRequestDto.*;
import static com.duckstar.web.dto.admin.SubmissionResponseDto.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Validated
public class AdminController {

    private final WeekVoteSubmissionRepository weekVoteSubmissionRepository;

    private final ShadowBanService shadowBanService;
    private final SubmissionService submissionService;
    private final AdminActionLogService adminActionLogService;
    private final CsvImportService csvImportService;
    private final ChartService chartService;
    private final AnimeCommandService animeCommandService;
    private final WeekService weekService;
    private final EpisodeQueryService episodeQueryService;
    private final EpisodeCommandService episodeCommandService;
    private final AnimeQueryService animeQueryService;

    @Operation(summary = "매니저 관리 로그 조회 API", description = "커서 기반 무한 스크롤")
    @GetMapping("/logs")
    public ApiResponse<ManagementLogSliceDto> getAdminLogsOnIpManagement(
            @RequestParam ManageFilterType filterType,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                adminActionLogService.getManagementLogs(filterType, pageable));
    }

    /**
     * 애니메이션 데이터 관리
     */
    // 모든 분기 조회 API : GET /api/v1/search/quarters 재사용

    @GetMapping("/animes")
    public ApiResponse<AdminAnimeListDto> getAnimes(
            @RequestParam Long quarterId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.onSuccess(
                animeQueryService.getAdminAnimeListDto(quarterId, pageable));
    }

    @Operation(summary = "애니메이션 등록 API")
    @PostMapping("/animes")
    public ApiResponse<Long> createAnime(
            @AuthenticationPrincipal MemberPrincipal principal,
            @Valid @ModelAttribute PostRequestDto request
    ) throws IOException {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                animeCommandService.createAnime(memberId, request));
    }

    @Operation(summary = "애니메이션 정보 수정 API",
            description = "TVA의 방향 업데이트, 애니메이션의 상태, 제작사 업데이트")
    @PatchMapping("/animes/{animeId}")
    public ApiResponse<List<ManagerProfileDto>> updateInfo(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long animeId,
            @Valid @RequestBody InfoRequestDto request
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                animeCommandService.updateInfo(
                        memberId,
                        animeId,
                        request,
                        LocalDateTime.now()
                ));
    }

    //TODO 애니메이션 정보 수정 API
    // - 단순 수정
    // - airTime, premiereDateTime 데이터 일치(정규화 없이) 생각

    @Operation(summary = "애니메이션 총 화수 수정 API")
    @PostMapping("/animes/{animeId}/total-episodes")
    public ApiResponse<EpisodeManageResultDto> updateTotalEpisodes(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long animeId,
            @Valid @RequestBody TotalEpisodesRequestDto request
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                animeCommandService.updateTotalEpisodes(memberId, animeId, request));
    }

    @Operation(summary = "애니메이션 총 화수 알 수 없음 Set API")
    @PatchMapping("/{animeId}/total-episodes/unknown")
    public ApiResponse<EpisodeManageResultDto> updateTotalEpisodes(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long animeId
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                animeCommandService.setUnknown(memberId, animeId));
    }

    @Operation(summary = "애니메이션 메인 이미지 수정 API")
    @PostMapping("/animes/{animeId}")
    public ApiResponse<Long> updateAnimeImage(
            @PathVariable Long animeId,
            @ModelAttribute ImageRequestDto request
    ) throws IOException {
        return ApiResponse.onSuccess(
                animeCommandService.updateAnimeImage(animeId, request));
    }

    /**
     * 시간표 관리 (에피소드 데이터 관리)
     */
    @Operation(summary = "애니메이션 별 에피소드 조회")
    @GetMapping("/animes/{animeId}/episodes")
    public ApiResponse<AdminEpisodeListDto> getEpisodesByAnime(@PathVariable Long animeId) {
        return ApiResponse.onSuccess(
                episodeQueryService.getAdminEpisodesByAnimeId(animeId));
    }

    @Operation(summary = "에피소드 추가(큐잉) API", description = "큐잉만 가능 - Tail(끝) 에피소드 추가 방식")
    @PostMapping("/animes/{animeId}/episodes")
    public ApiResponse<EpisodeManageResultDto> queueEpisode(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long animeId
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                episodeCommandService.queueEpisode(memberId, animeId));
    }

    // 모든 주차 조회 API : GET /api/v1/chart/weeks 재사용

    @Operation(summary = "주간(월 18시 정책 기준) 에피소드 조회")
    @GetMapping("/weeks/{weekId}")
    public ApiResponse<AdminScheduleInfoDto> getEpisodes(@PathVariable Long weekId) {
        return ApiResponse.onSuccess(
                episodeQueryService.getAdminScheduleByWeekId(weekId));
    }

    @Operation(summary = "에피소드 정보 수정 API")
    @PatchMapping("/episodes/{episodeId}")
    public ApiResponse<List<ManagerProfileDto>> rescheduleEpisode(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long episodeId,
            ModifyRequestDto request
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                episodeCommandService.modifyEpisode(memberId, episodeId, request));
    }

    @Operation(summary = "에피소드 휴방 API")
    @PostMapping("/episodes/{episodeId}")
    public ApiResponse<EpisodeManageResultDto> breakEpisode(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long episodeId
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                episodeCommandService.breakEpisode(memberId, episodeId));
    }

    @Operation(summary = "에피소드 삭제 API",
            description = "다음 주부터의 에피소드만 삭제 가능")
    @DeleteMapping("/episodes/{episodeId}")
    public ApiResponse<ManagerProfileDto> deleteMoreThanNextWeekEpisode(
            @AuthenticationPrincipal MemberPrincipal principal,
            @PathVariable Long episodeId
    ) {
        Long memberId = principal == null ? null : principal.getId();
        return ApiResponse.onSuccess(
                episodeCommandService.deleteMoreThanNextWeekEpisode(memberId, episodeId, LocalDateTime.now()));
    }

    /**
     * 제출 현황 관리
     */
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

    @Operation(summary = "편의용 주간 마감 API",
            description = "주간 덕스타 차트 계산, AniLab 차트 csv 읽고 등록")
    @PostMapping(value = "/chart/{year}/{quarter}/{week}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> calculateRankAndImportAniLab(
            @PathVariable Integer year,
            @PathVariable Integer quarter,
            @PathVariable Integer week,
            @ModelAttribute AbroadRequestDto request
    ) throws IOException {
        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        // 바로 발표 준비 완료됨 주의
        chartService.calculateRankByYQW(weekId);

        csvImportService.importAnimeCorner(weekId, request.getAnimeCornerCsv());
        csvImportService.importAnilab(weekId, request.getAnilabCsv());

        return ApiResponse.onSuccess(null);
    }
}
