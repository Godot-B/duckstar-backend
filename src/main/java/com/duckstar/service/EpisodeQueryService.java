package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.domain.Week;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.security.MemberPrincipal;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.duckstar.web.dto.VoteResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeQueryService {

    private final EpisodeRepository episodeRepository;

    private final VoteCookieManager voteCookieManager;

    private final WeekService weekService;

    /**
     * 별점 투표 방식
     */
    public LiveCandidateListDto getLiveCandidatesByWindow(
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        // 사용자의 principal_key 최대 2개 (2주 걸치는 시간 존재)
        List<String> cookies = voteCookieManager.readAllCookies(requestRaw);
        List<String> principalKeys = cookies.isEmpty() ?
                List.of(voteCookieManager.toPrincipalKey(memberId, null)) :
                cookies.stream()
                        .map(c -> voteCookieManager.toPrincipalKey(memberId, c))
                        .toList();

        // 전체 VOTING_WINDOW 상태 에피소드들 조회
        List<LiveCandidateDto> candidates = episodeRepository
                .getLiveCandidateDtos(principalKeys);

        LocalDateTime now = LocalDateTime.now();
        Week currentWeek = weekService.getWeekByTime(now);
        Integer currentWeekValue = currentWeek.getWeekValue();

        // 분리: 이번 주
        List<LiveCandidateDto> currentWeekStarCandidates =
                candidates.stream()
                        .filter(c -> c.getWeek().equals(currentWeekValue))
                        .toList();

        // 분리: 지난 주
        List<LiveCandidateDto> lastWeekStarCandidates =
                candidates.stream()
                        .filter(c -> !c.getWeek().equals(currentWeekValue))
                        .toList();

        return LiveCandidateListDto.builder()
                .weekDto(WeekDto.of(currentWeek))
                .currentWeekLiveCandidates(currentWeekStarCandidates)
                .lastWeekLiveCandidates(lastWeekStarCandidates)
                .build();
    }

    public List<WeekCandidateDto> getWeekCandidatesByYQW(
            Integer year,
            Integer quarter,
            Integer week,
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        String cookieId = voteCookieManager.readCookie(
                requestRaw,
                year,
                quarter,
                week
        );
        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        Long weekId = weekService.getWeekIdByYQW(year, quarter, week);

        return episodeRepository.getWeekCandidateDtos(weekId, principalKey);
    }

    public CandidateFormDto getCandidateForm(
            Long episodeId,
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        // 사용자의 principal_key 최대 2개 (2주 걸치는 시간 존재)
        List<String> cookies = voteCookieManager.readAllCookies(requestRaw);
        List<String> principalKeys = cookies.isEmpty() ?
                List.of(voteCookieManager.toPrincipalKey(memberId, null)) :
                cookies.stream()
                        .map(c -> voteCookieManager.toPrincipalKey(memberId, c))
                        .toList();

        return episodeRepository.getCandidateFormDto(episodeId, principalKeys)
                .orElseThrow(() -> new EpisodeHandler(ErrorStatus.EPISODE_NOT_FOUND));
    }
}
