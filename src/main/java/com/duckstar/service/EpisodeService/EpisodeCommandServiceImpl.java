package com.duckstar.service.EpisodeService;

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
public class EpisodeCommandServiceImpl implements EpisodeCommandService {
    private final EpisodeRepository episodeRepository;

    public void updateAllEpisodeStates() {
        LocalDateTime now = LocalDateTime.now();
        List<Episode> allEpisodes = episodeRepository.findAll();

        allEpisodes.forEach(episode -> {
            LocalDateTime scheduledAt = episode.getScheduledAt();
            LocalDateTime votingWindowEndedAt = scheduledAt.plusHours(36);

            if (now.isAfter(votingWindowEndedAt)) {
                episode.setEvaluateState(EpEvaluateState.LOGIN_REQUIRED);
            } else if (now.isAfter(scheduledAt)) {
                episode.setEvaluateState(EpEvaluateState.VOTING_WINDOW);
            } else {
                episode.setEvaluateState(EpEvaluateState.CLOSED);
            }
        });
    }
}
