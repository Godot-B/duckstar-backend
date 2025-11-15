package com.duckstar.service;

import com.duckstar.apiPayload.code.status.ErrorStatus;
import com.duckstar.apiPayload.exception.handler.EpisodeHandler;
import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
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
        Week week = weekService.getWeekByTime(now);

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

        boolean isBlocked = false;

        Map<Long, StarInfoDto> userStarInfoMap;
        if (submissionOpt.isPresent()) {  // 투표 내역 존재
            userStarInfoMap = new HashMap<>();
            WeekVoteSubmission submission = submissionOpt.get();
            isBlocked = submission.isBlocked();

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

        } else {
            userStarInfoMap = Map.of();
        }

        //=== 월 18시 ~ 화 15시에는 지난 주차와 공존하는 경우 있으므로 확인 ===//
        Map<Long, StarInfoDto> lastUserStarInfoMap;
        LocalDateTime hybridStart = week.getStartDateTime();  // 이번 주 월요일 18시
        LocalDateTime hybridEnd = hybridStart.with(DayOfWeek.TUESDAY).withHour(15).withMinute(0);  // 화요일 15시
        boolean isHybrid = !now.isBefore(hybridStart) && now.isBefore(hybridEnd);

        if (isHybrid) {
            Week lastWeek = weekService.getWeekByTime(week.getStartDateTime().minusWeeks(1));
            Quarter lastWeekQuarter = lastWeek.getQuarter();
            String lastCookieId = voteCookieManager.readCookie(
                    requestRaw,
                    lastWeekQuarter.getYearValue(),
                    lastWeekQuarter.getQuarterValue(),
                    lastWeek.getWeekValue()
            );
            String lastPrincipalKey = voteCookieManager.toPrincipalKey(memberId, lastCookieId);

            Optional<WeekVoteSubmission> lastSubmissionOpt =
                    submissionRepository.findByWeek_IdAndPrincipalKey(
                            lastWeek.getId(), lastPrincipalKey);

            if (lastSubmissionOpt.isPresent()) { // 지난 주 투표 내역 존재
                lastUserStarInfoMap = new HashMap<>();
                WeekVoteSubmission submission = lastSubmissionOpt.get();
                isBlocked = submission.isBlocked();

                Map<Episode, Integer> episodeMap =
                        episodeStarRepository.findEpisodeMapBySubmissionId(submission.getId());

                for (Map.Entry<Episode, Integer> entry : episodeMap.entrySet()) {
                    Episode episode = entry.getKey();
                    lastUserStarInfoMap.put(
                            episode.getId(),
                            StarInfoDto.of(
                                    isBlocked,
                                    entry.getValue(),
                                    episode
                            )
                    );
                }

            } else {
                lastUserStarInfoMap = Map.of();
            }
        } else {
            lastUserStarInfoMap = Map.of();
        }

        List<StarCandidateDto> starCandidates =
                episodeRepository.getStarCandidatesByDuration(
                        now.minusHours(36),
                        now
                );

        starCandidates.forEach(sc -> {
            Long episodeId = sc.getEpisodeId();
            if (episodeId != null) {
                StarInfoDto info = userStarInfoMap.get(episodeId);
                sc.setUserHistory(info);

                // 지난 주차도 확인
                if (info == null && isHybrid) {
                    info = lastUserStarInfoMap.get(episodeId);
                    sc.setUserHistory(info);
                }
            }
        });

        return StarCandidateListDto.builder()
                .weekDto(WeekResponseDto.WeekDto.of(week))
                .starCandidates(starCandidates)
                .build();
    }
}
