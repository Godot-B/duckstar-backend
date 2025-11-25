package com.duckstar.service;

import com.duckstar.domain.Quarter;
import com.duckstar.domain.Week;
import com.duckstar.domain.enums.EpEvaluateState;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.domain.mapping.WeekVoteSubmission;
import com.duckstar.repository.Episode.EpisodeRepository;
import com.duckstar.repository.EpisodeStar.EpisodeStarRepository;
import com.duckstar.repository.WeekVoteSubmission.WeekVoteSubmissionRepository;
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
        List<String> cookies = voteCookieManager.readAllCookies(requestRaw);

        List<String> principalKeys = cookies.stream()
                .map(c -> voteCookieManager.toPrincipalKey(memberId, c))
                .toList();

        List<StarCandidateDto> candidates = episodeRepository
                .getStarCandidatesOnVotingWindow(principalKeys);

        LocalDateTime now = LocalDateTime.now();
        Week currentWeek = weekService.getWeekByTime(now);
        Integer currentWeekValue = currentWeek.getWeekValue();

        // 분리: 이번 주
        List<StarCandidateDto> currentWeekStarCandidates =
                candidates.stream()
                        .filter(c -> c.getWeek().equals(currentWeekValue))
                        .toList();

        // 분리: 지난 주
        List<StarCandidateDto> lastWeekStarCandidates =
                candidates.stream()
                        .filter(c -> !c.getWeek().equals(currentWeekValue))
                        .toList();

        return StarCandidateListDto.builder()
                .weekDto(WeekDto.of(currentWeek))
                .currentWeekStarCandidates(currentWeekStarCandidates)
                .lastWeekStarCandidates(lastWeekStarCandidates)
                .build();
    }

    public List<WeekCandidateDto> getWeekCandidatesByYQW() {
        return null;
    }

}
