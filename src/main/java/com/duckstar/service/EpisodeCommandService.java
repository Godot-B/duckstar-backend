package com.duckstar.service;

import com.duckstar.domain.Week;
import com.duckstar.domain.enums.EpEvaluateState;
import com.duckstar.domain.mapping.Episode;
import com.duckstar.repository.Episode.EpisodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EpisodeCommandService {
    private final EpisodeRepository episodeRepository;
    private final WeekService weekService;

    public void updateAllEpisodeStates() {
        LocalDateTime now = LocalDateTime.now();
        Week week = weekService.getWeekByTime(now);
        LocalDateTime lastWeekEndedAt = week.getStartDateTime();

        List<Episode> allEpisodes = episodeRepository.findAll();

        allEpisodes.forEach(episode -> {
            LocalDateTime scheduledAt = episode.getScheduledAt();
            LocalDateTime votingWindowEndedAt = scheduledAt.plusHours(36);

            // 이번 주가 아닌 에피소드
            if (scheduledAt.isBefore(lastWeekEndedAt)) {
                episode.setEvaluateState(EpEvaluateState.ALWAYS_OPEN);

            // 이번 주 에피소드
            } else if (now.isAfter(votingWindowEndedAt)) {
                episode.setEvaluateState(EpEvaluateState.LOGIN_REQUIRED);
            } else if (now.isAfter(scheduledAt)) {
                episode.setEvaluateState(EpEvaluateState.VOTING_WINDOW);
            } else {
                episode.setEvaluateState(EpEvaluateState.CLOSED);
            }

        });
    }
}
