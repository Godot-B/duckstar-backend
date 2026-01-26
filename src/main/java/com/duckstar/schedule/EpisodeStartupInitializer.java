package com.duckstar.schedule;

import com.duckstar.service.EpisodeService.EpisodeCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class EpisodeStartupInitializer {

    private final EpisodeCommandService episodeCommandService;
    private final ScheduleHandler scheduleHandler;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        episodeCommandService.updateAllEpisodeStates();

        // 이번 주 Week 없다면 생성
        LocalDateTime now = LocalDateTime.now();
        scheduleHandler.getSafeWeekByTime(now);

        // 지난 주 Week 없다면 생성
        LocalDateTime nowMinusWeek = LocalDateTime.now().minusWeeks(1);
        scheduleHandler.getSafeWeekByTime(nowMinusWeek);
    }
}