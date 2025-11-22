package com.duckstar.service;

import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.EpEvaluateState;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
import com.duckstar.web.dto.WeekResponseDto;
import com.duckstar.web.support.VoteCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.duckstar.web.dto.VoteResponseDto.*;
import static com.duckstar.web.dto.WeekResponseDto.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EpisodeQueryService {

    private final WeekVoteSubmissionRepository submissionRepository;
    private final EpisodeRepository episodeRepository;
    private final EpisodeStarRepository episodeStarRepository;

    private final VoteCookieManager voteCookieManager;

    private final WeekService weekService;

    /**
     * 별점 투표 방식
     */
    public StarCandidateListDto getStarCandidatesByWindow(
            Long memberId,
            HttpServletRequest requestRaw
    ) {
        LocalDateTime now = LocalDateTime.now();

        //=== 이번 주 ===//
        Week currentWeek = weekService.getWeekByTime(now);
        List<StarCandidateDto> currentCandidates = getStarCandidatesByWeek(memberId, requestRaw, currentWeek);

        //=== 월 18시 ~ 화 15시: DB look up (지난 주차 마지막 후보의 방영 시간 체크) ===//
        LocalDateTime hybridStart = currentWeek.getStartDateTime();
        LocalDateTime hybridEnd = hybridStart.with(DayOfWeek.TUESDAY)
                .withHour(15)
                .withMinute(0);
        boolean isHybrid = false;
        if (!now.isBefore(hybridStart) && now.isBefore(hybridEnd)) {
            isHybrid = episodeRepository.isHybridTime(currentWeek, now);
        }

        //=== 지난 주 ===//
        List<StarCandidateDto> lastCandidates = null;
        if (isHybrid) {
            Week lastWeek = weekService.getWeekByTime(currentWeek.getStartDateTime().minusWeeks(1));
            lastCandidates = getStarCandidatesByWeek(memberId, requestRaw, lastWeek);
        }

        return StarCandidateListDto.builder()
                .weekDto(WeekDto.of(currentWeek))
                .currentWeekStarCandidates(currentCandidates)
                .lastWeekStarCandidates(lastCandidates)
                .build();
    }

    private List<StarCandidateDto> getStarCandidatesByWeek(
            Long memberId,
            HttpServletRequest requestRaw,
            Week week
    ) {
        //=== 주차 후보 조회 ===//
        List<StarCandidateDto> candidates = episodeRepository.getStarCandidatesByWeek(week);

        //=== 투표 내역 검색 ===//
        Quarter quarter = week.getQuarter();
        String cookieId = voteCookieManager.readCookie(
                requestRaw,
                quarter.getYearValue(),
                quarter.getQuarterValue(),
                week.getWeekValue()
        );

        String principalKey = voteCookieManager.toPrincipalKey(memberId, cookieId);

        Optional<WeekVoteSubmission> submissionOpt =
                submissionRepository.findByWeek_IdAndPrincipalKey(
                        week.getId(), principalKey);

        Map<Long, StarInfoDto> userStarInfoMap;
        if (submissionOpt.isPresent()) {  // 투표 내역 존재
            userStarInfoMap = new HashMap<>();
            WeekVoteSubmission submission = submissionOpt.get();
            boolean isBlocked = submission.isBlocked();

            Map<Episode, Integer> episodeMap =
                    episodeStarRepository.findEpisodeMapBySubmissionId(submission.getId());

            for (Map.Entry<Episode, Integer> entry : episodeMap.entrySet()) {
                Episode episode = entry.getKey();
                userStarInfoMap.put(
                        episode.getId(),
                        StarInfoDto.of(
                                isBlocked,
                                entry.getValue(),
                                episode
                        )
                );
            }

            //=== 주차 후보에 투표 내역 셋팅 ===//
            candidates.forEach(sc -> {
                Long episodeId = sc.getEpisodeId();
                if (episodeId != null) {
                    EpEvaluateState state = sc.getState();
                    StarInfoDto info = userStarInfoMap.get(episodeId);
                    if (info != null) {
                        // 실시간 투표 후보
                        if (state == EpEvaluateState.VOTING_WINDOW) {
                            sc.setUserHistory(info);
                        // 투표 시간 지난 후보
                        } else if (state == EpEvaluateState.LOGIN_REQUIRED) {
                            Integer userStarScore = info.getUserStarScore();
                            boolean hasVoted = userStarScore != null;
                            sc.setVoted(hasVoted);
                        }
                    }
                }
            });
        }

        return candidates;
    }
}
